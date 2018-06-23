package org.rock.vtcache.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rock.vtcache.domain.Tag;
import org.rock.vtcache.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping("/api/tags")
public class VtcacheController {

    @Autowired
    TagRepository tagRepository;

    /**
     * Route to store VMware tags. Deletes all tags before storing new ones.
     * @param json JSON String passed in the request body
     */
    @PostMapping(value="/store", consumes = APPLICATION_JSON_VALUE)
    public void tagJson(@RequestBody String json) {
        // delete any existing tags
        tagRepository.deleteAll();

        // The Ansible 'vmware_tags_facts' python dict output is similar to
        // JSON, it just needs a little massaging
        String fixed = makeJsonGreatAgain(json);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Tag[] tags = mapper.readValue(fixed, Tag[].class);
            for (Tag t : tags)
                tagRepository.save(t);
        } catch (Exception exception) {
            exception.printStackTrace();
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

    /**
     * Forces python dict strings returned from the Ansible 'vmware_tags_facts' module
     * into a format that can be understood by Jackson.
     *
     * @param pydict  String passed from the Ansible 'vmware_tags_facts' module
     * @return String containing a properly-formatted JSON array of tags
     */
    private String makeJsonGreatAgain(String json) {
        //firstly, remove the leading '{' from the string
        String ret = "";
        String str = json.substring(1);

        // split the string
        String[] strings = str.split("},");
        for(String s : strings) {
            // now, remove any closing brackets
            s.replaceAll("}", "");

            // move the name element inside the brackets
            Pattern patt = Pattern.compile("\'(\\w*)\':\\s*\\{");
            Matcher m = patt.matcher(s);
            if (m.find()) {
                String out = m.replaceFirst("{ \'name\': \'$1\',");  // number 46
                ret += out + "},";
            }
        }

        //strip the last two brackets and comma
        ret = ret.substring(0,ret.length()-3);
        //replace all single-quotes with double-quptes
        ret = ret.replaceAll("\'","\"");
        //add array markers
        ret = "["+ret+"]";
        return ret;
    }

}
