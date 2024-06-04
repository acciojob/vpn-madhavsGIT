package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {

        //Connect the user to a vpn by considering the following priority order.
        //1. If the user is already connected to any service provider, throw "Already connected" exception.
        //2. Else if the countryName corresponds to the original country of the user, do nothing.
        // This means that the user wants to connect to its original country, for which we do not require a connection.
        // Thus, return the user as it is.
        //3. Else, the user should be subscribed under a serviceProvider having option to connect to the given country.
        //If the connection can not be made (As user does not have a serviceProvider or serviceProvider
        // does not have given country, throw "Unable to connect" exception.
        //Else, establish the connection where the maskedIp is "updatedCountryCode.serviceProviderId.userId" and
        // return the updated user. If multiple service providers allow you to connect to the country,
        // use the service provider having smallest id.

        //get user
        User user = userRepository2.findById(userId).get();

        //already connection exception
        if (user.getMaskedIp() != null) {
            throw new Exception("Already connected");
        }
        // 2.
        else if (countryName.equalsIgnoreCase(user.getOriginalCountry().getCountryName().toString())) {
            return user;
        }
        //3.
        else {
            if (user.getServiceProviderList() == null) {
                throw new Exception("Unable to connect");
            }
            List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
            int minId = Integer.MAX_VALUE;
            ServiceProvider avaibleProvider = null;
            Country country2 = null;
            for (ServiceProvider provider : serviceProviderList) {
                List<Country> countryList = provider.getCountryList();
                for (Country country : countryList) {
                    if (country.getCountryName().toString().equalsIgnoreCase(countryName)) {
                        if (provider.getId() < minId) {
                            minId = provider.getId();
                            avaibleProvider = provider;
                            country2 = country;
                        }
                    }
                }
            }
            if (avaibleProvider != null) {
                Connection connection = new Connection();
                connection.setUser(user);
                connection.setServiceProvider(avaibleProvider);

                //maskedIP - "updatedCountryCode.serviceProviderId.userId"

                String maskedIp = country2.getCode() + "." + avaibleProvider.getId() + "." + userId;
                user.setMaskedIp(maskedIp);
                user.setConnected(true);
                user.getConnectionList().add(connection);
                avaibleProvider.getConnectionList().add(connection);

                userRepository2.save(user);
                serviceProviderRepository2.save(avaibleProvider);

                return user;

            }
            else{
                throw new Exception("Unable to connect");
            }

        }
    }
    @Override
    public User disconnect(int userId) throws Exception {
        //If the given user was not connected to a vpn, throw "Already disconnected" exception.
        //Else, disconnect from vpn, make masked Ip as null, update relevant attributes
        // and return updated user


        User user = userRepository2.findById(userId).get();
        if(user.getConnected() == false){
            throw new Exception("Already disconnected");
        }

            user.setConnected(false);
            user.setMaskedIp(null);

            userRepository2.save(user);
            return user;


    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        //Establish a connection between sender and receiver users
        //To communicate to the receiver, sender should be in the current country of the receiver.
        //If the receiver is connected to a vpn, his current country is the one he is connected to.
        //If the receiver is not connected to vpn, his current country is his original country.

        //The sender is initially not connected to any vpn.
        // If the sender's original country does not match receiver's current country,
        // we need to connect the sender to a suitable vpn.
        // If there are multiple options, connect using the service provider having smallest id

        //If the sender's original country matches receiver's current country, we do not need to do anything as they can communicate. Return the sender as it is.
        //If communication can not be established due to any reason, throw "Cannot establish communication" exception


        //get sender and user

        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

       if(receiver.getMaskedIp() != null) {
           String ip = receiver.getMaskedIp();
           String code = ip.substring(0,3);
           if(code.equalsIgnoreCase(sender.getOriginalCountry().getCode())){
               return sender;
           }
           else {
              String countryName = "";

              if(CountryName.IND.toCode().equalsIgnoreCase(code)){
                  countryName = "IND";
              }
               if(CountryName.USA.toCode().equalsIgnoreCase(code)){
                   countryName = "USA";
               }
               if(CountryName.AUS.toCode().equalsIgnoreCase(code)){
                   countryName = "AUS";
               }
               if(CountryName.JPN.toCode().equalsIgnoreCase(code)){
                   countryName = "JPN";
               }
               if(CountryName.CHI.toCode().equalsIgnoreCase(code)){
                   countryName = "CHI";
               }

               try{
                   User updatedSender = connect(senderId, countryName);
                   return updatedSender;
               }
               catch (Exception e){
                   throw new Exception("Cannot establish communication");
               }
           }
       }else{
          if( sender.getOriginalCountry().equals(receiver.getOriginalCountry()))
           return sender;
           else {
               String countryname = receiver.getOriginalCountry().getCountryName().toString();
               try{
                   User updatedSender = connect(senderId, countryname);
                   return updatedSender;
               }
               catch (Exception e){
                   throw new Exception("Cannot establish communication");
               }
           }

       }

    }
}
