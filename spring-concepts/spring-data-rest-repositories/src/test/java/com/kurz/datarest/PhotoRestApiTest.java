package com.kurz.datarest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PhotoRestLabApplication.class)
@AutoConfigureMockMvc
@DisplayName("Photo REST API (Spring Data REST)")
class PhotoRestApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PhotoRepository photoRepository;

    @BeforeEach
    void setUp() {
        photoRepository.deleteAll();
        photoRepository.save(new Photo("Golden Gate at dusk", "Ansel Ada"));
        photoRepository.save(new Photo("Times Square at night", "Ansel Ada"));
        photoRepository.save(new Photo("Quiet lake", "Rae Lin"));
    }

    @Test
    @DisplayName("GET /photos returns a HAL collection with self links and _embedded.photos")
    void collectionResourceIsHalShaped() throws Exception {
        mockMvc.perform(get("/photos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._embedded.photos").isArray())
                .andExpect(jsonPath("$._embedded.photos.length()").value(3))
                .andExpect(jsonPath("$._embedded.photos[0]._links.self.href").exists());
    }

    @Test
    @DisplayName("the pluralization fix makes /photos (not /photoes) the collection resource")
    void collectionUsesFixedPluralPath() throws Exception {
        mockMvc.perform(get("/photos")).andExpect(status().isOk());
        mockMvc.perform(get("/photoes")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /photos creates a resource with a self link and an assigned id")
    void postCreatesResourceWithSelfLink() throws Exception {
        String body = """
                {"caption":"New shot","photographer":"Rae Lin"}""";

        mockMvc.perform(post("/photos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$.caption").value("New shot"));
    }

    @Test
    @DisplayName("findByPhotographerIgnoreCase is exposed as a search resource")
    void customFinderExposedAsSearchResource() throws Exception {
        mockMvc.perform(get("/photos/search/findByPhotographerIgnoreCase")
                        .param("photographer", "ansel ada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.photos.length()").value(2))
                .andExpect(jsonPath("$._embedded.photos[0].photographer").value("Ansel Ada"));
    }
}
