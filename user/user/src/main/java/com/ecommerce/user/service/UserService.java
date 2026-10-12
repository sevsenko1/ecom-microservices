package com.ecommerce.user.service;


import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.model.Address;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KeyCloakAdminService keyCloakAdminService;
    //private Long nextId=1L;
    //private List<User> userList=new ArrayList<>();

    /*public UserService(UserRepository userRepository) { required args constructor
        this.userRepository = userRepository;
    }*/



    public List<UserResponse> fetchAllUsers(){
       // List<User> userList= userRepository.findAll();
        return userRepository.findAll().stream()
                .map(this :: mapToUserResponse)
                .collect(Collectors.toList());
    }

    public void addUser( UserRequest userRequest){
       // user.setId(nextId++);
        String token= keyCloakAdminService.getAdminAccessToken();
        String keycloakUserId=keyCloakAdminService.createUser(token,userRequest);


        User user =new User();
        updateUserFromRequest(user,userRequest);
        user.setKeycloakId(keycloakUserId);
        userRepository.save(user);

    }

    private void updateUserFromRequest(User user, UserRequest userRequest) {
        if (userRequest.getFirstName() != null && !userRequest.getFirstName().isBlank()) {
            user.setFirstName(userRequest.getFirstName());
        }

        if (userRequest.getLastName() != null && !userRequest.getLastName().isBlank()) {
            user.setLastName(userRequest.getLastName());
        }

        if (userRequest.getEmail() != null && !userRequest.getEmail().isBlank()) {
            user.setEmail(userRequest.getEmail());
        }

        if (userRequest.getPhone() != null && !userRequest.getPhone().isBlank()) {
            user.setPhone(userRequest.getPhone());
        }
        if(userRequest.getAddress()!=null){
            Address address=new Address();
            address.setStreet(userRequest.getAddress().getStreet());
            address.setCity(userRequest.getAddress().getCity());
            address.setZipcode(userRequest.getAddress().getZipcode());
            address.setState(userRequest.getAddress().getState());
            address.setCountry(userRequest.getAddress().getCountry());
            user.setAddress(address);
        }
    }

    public Optional<UserResponse> fetchUser(String id) {
        /*return userList.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();*/
        return userRepository.findById((id))
                .map(this :: mapToUserResponse);
    }

    public boolean updateUser(String id,UserRequest updatedUserRequest){
        return userRepository.findById((id))
                .map(existingUser -> {
                    updateUserFromRequest(existingUser,updatedUserRequest);
                    userRepository.save(existingUser);
                    return true;
                }).orElse(false);


    }
    private UserResponse mapToUserResponse(User user){
        UserResponse response=new UserResponse();
        response.setKeycloakId(user.getKeycloakId());
        response.setId(String.valueOf(user.getId()));
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());

        if(user.getAddress()!=null){
            AddressDTO addressDTO=new AddressDTO();
            addressDTO.setStreet(user.getAddress().getStreet());
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setState(user.getAddress().getState());
            addressDTO.setCountry(user.getAddress().getCountry());
            addressDTO.setZipcode(user.getAddress().getZipcode());
            response.setAddress(addressDTO);
        }

        return response;
    }
}
