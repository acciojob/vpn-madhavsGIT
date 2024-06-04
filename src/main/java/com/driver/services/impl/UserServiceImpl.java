package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{

        //create a user of given country.
        // The originalIp of the user should be "countryCode.userId" and
        // return the user. Note that right now user is not connected and
        // thus connected would be false and maskedIp would be null
        //Note that the userId is created automatically by the repository layer



        User user = new User();
        user.setPassword(password);
        user.setUsername(username);

        if(countryName.equalsIgnoreCase("IND") ||
                countryName.equalsIgnoreCase("USA")||
                countryName.equalsIgnoreCase("AUS")||
                countryName.equalsIgnoreCase("CHI")||
                countryName.equalsIgnoreCase("JPN")) {

            Country country = new Country();
            if(countryName.equalsIgnoreCase("IND")){
                country.setCountryName(CountryName.IND);
                country.setCode(CountryName.IND.toCode());
            }
            if(countryName.equalsIgnoreCase("USA")){
                country.setCountryName(CountryName.USA);
                country.setCode(CountryName.USA.toCode());
            }
            if(countryName.equalsIgnoreCase("AUS")){
                country.setCountryName(CountryName.AUS);
                country.setCode(CountryName.AUS.toCode());
            }
            if(countryName.equalsIgnoreCase("CHI")){
                country.setCountryName(CountryName.CHI);
                country.setCode(CountryName.CHI.toCode());
            }
            if(countryName.equalsIgnoreCase("JPN")){
                country.setCountryName(CountryName.JPN);
                country.setCode(CountryName.JPN.toCode());
            }
            country.setUser(user);
            user.setOriginalCountry(country);
            user.setConnected(false);


            String ip = country.getCode()+ "." + userRepository3.save(user).getId();
            user.setOriginalIp(ip);
            userRepository3.save(user);

            return user;

        }
        else{
            throw new Exception("Country not found");
        }
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {

        User user = userRepository3.findById(userId).get();
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();
        user.getServiceProviderList().add(serviceProvider);

        serviceProvider.getUsers().add(user);
        serviceProviderRepository3.save(serviceProvider);

        return user;
    }
}
