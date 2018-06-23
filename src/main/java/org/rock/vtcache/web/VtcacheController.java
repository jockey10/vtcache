package org.rock.vtcache.web;

import org.rock.vtcache.domain.Tag;
import org.rock.vtcache.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping("/api/tags")
public class VtcacheController {

    @Autowired
    TagRepository tagRepository;

    /**
     * Route to store VMware tags. Deletes all tags before storing new ones.
     * @param tags Map of Tag name and object data from JSON
     */
    @PostMapping(value="/store", consumes = APPLICATION_JSON_VALUE)
    public void tagJson(@RequestBody Map<String, Tag> tags) {

        // delete any existing tags
        tagRepository.deleteAll();

        for (Map.Entry<String, Tag> entry : tags.entrySet()) {
            String name = entry.getKey();
            Tag tag = entry.getValue();

            tag.setName(name);
            tagRepository.save(tag);
        }
    }

    /**
     * Route to retrieve tags from the H2 in-memory database.
     * @return a JSON array of @Tag objects
     */
    @GetMapping(value="/retrieve", produces=APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Tag>> getTags(){
        Iterable<Tag> tags = tagRepository.findAll();
        ArrayList<Tag> to_json = new ArrayList<>();
        tags.forEach(to_json::add);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(to_json,headers,HttpStatus.OK);
    }
}
