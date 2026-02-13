package com.kdichter.security.service;

import com.kdichter.security.contact.Contact;
import com.kdichter.security.contact.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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

    public Page<Contact> getAllContacts(int page, int size) {
        return contactRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
    }

    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact) {
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
                    .path("/contacts/image/" + filename)
                    .toString();

        } catch (Exception exception) {
            throw new RuntimeException("Unable to save image");
        }
    };
}
