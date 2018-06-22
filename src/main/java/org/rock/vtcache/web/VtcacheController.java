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

    @PostMapping(value="/store", consumes = APPLICATION_JSON_VALUE)
    public void tagJson(@RequestBody String json) {
        // delete any existing tags
        tagRepository.deleteAll();

        // the json returned by Ansible is horrible - let's fix it
        // so that java can handle it...
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

    @GetMapping(value="/retrieve", produces=APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Tag>> getTags(){
        // get the first item
        Iterable<Tag> tags = tagRepository.findAll();
        ArrayList<Tag> to_json = new ArrayList<>();
        tags.forEach(to_json::add);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(to_json,headers,HttpStatus.OK);
    }

    public String makeJsonGreatAgain(String json) {
        //firstly, remove the leading '{' from the string
        String ret = "";
        String str = json.substring(1);

        // split the string
        String[] strings = str.split("},");
        for(String s : strings) {
            System.out.println(s);
            // now, remove any closing brackets
            s.replaceAll("}", "");
            
            // move the name element inside the brackets
            Pattern patt = Pattern.compile("\'(\\w*)\':\\s*\\{");
            Matcher m = patt.matcher(s);
            if (m.find()) {
                String out = m.replaceFirst("{ \'name\': \'$1\',");  // number 46
                System.out.println(out);
                ret += out + "},";
            }
        }

        //strip the last two brackets and comma
        ret = ret.substring(0,ret.length()-3);
        ret = ret.replaceAll("\'","\"");
        System.out.println(ret);
        //add array markers
        ret = "["+ret+"]";
        return ret;
    }

}
