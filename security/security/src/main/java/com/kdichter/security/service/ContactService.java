package com.kdichter.security.service;

import com.kdichter.security.contact.Contact;
import com.kdichter.security.contact.ContactRepository;
import com.kdichter.security.user.User;
import com.kdichter.security.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.kdichter.security.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    // Get the currently logging-in user from JWT token
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public Page<Contact> getAllContacts(int page, int size) {
        User currentUser = getCurrentUser();
        return contactRepository.findByUser(currentUser, PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id) {
        User currentUser = getCurrentUser();
        return contactRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found or access denied"));
    }

    public Contact createContact(Contact contact) {
        User currentUser = getCurrentUser();
        contact.setUser(currentUser); // Associate with current user
        return contactRepository.save(contact);
    }

    public Contact updateContact(String id, Contact contact) {
        User currentUser = getCurrentUser();
        Contact existingContact = contactRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found or access denied"));

        existingContact.setName(contact.getName());
        existingContact.setEmail(contact.getEmail());
        existingContact.setTitle(contact.getTitle());
        existingContact.setPhone(contact.getPhone());
        existingContact.setAddress(contact.getAddress());
        existingContact.setStatus(contact.getStatus());

        return contactRepository.save(existingContact);
    }

    public void deleteContact(String id) {
        User currentUser = getCurrentUser();
        Contact contact = contactRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found or access denied"));
        contactRepository.delete(contact);
    }

    public String uploadPhoto(String id, MultipartFile file) {
        log.info("Saving picture for user ID: {}", id);
        Contact contact = getContact(id);
        String photoUrl = photoFunction.apply(id, file);
        contact.setPhotoUrl(photoUrl);
        contactRepository.save(contact);
        return photoUrl;
    }

    /**
     * **Flow:**
     * ```
     * uploadPhoto("123", profileImage)
     * ↓
     * Get Contact 123 from DB
     * ↓
     * photoFunction.apply("123", profileImage)
     * → Saves file as "123.jpg"
     * → Returns "http://localhost:8080/contacts/image/123.jpg"
     * ↓
     * contact.setPhotoUrl("http://localhost:8080/contacts/image/123.jpg")
     * ↓
     * Save contact to DB
     * ↓
     * Return URL
     */

    // private String fileExtension (String filename)
    private final Function<String, String> fileExtension = filename ->
            Optional.of(filename)
                    .filter(name -> name.contains("."))
                    .map(name -> "." + name.substring(filename.lastIndexOf(".") + 1))
                    .orElse(".png");

    // private String photoFunction(String id, MultipartFile, image)
    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        // 1. Create filename: id + extension
        String filename = id + fileExtension.apply(image.getOriginalFilename());
        try {
            // 2. Get/create storage directory
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY)
                    .toAbsolutePath()
                    .normalize();

            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }

            // 3. Save the file
            Files.copy(
                    image.getInputStream(),
                    fileStorageLocation.resolve(filename),
                    REPLACE_EXISTING
            );

            // 4. Build and return the URL
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/v1/contacts/image/" + filename)
                    .toUriString();

        } catch (Exception exception) {
            throw new RuntimeException("Unable to save image");
        }
    };
}
