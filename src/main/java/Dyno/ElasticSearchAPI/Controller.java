package Dyno.ElasticSearchAPI;

import ElasticSearchFB.FacebookSearcher;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
public class Controller {
    @GetMapping("/facebook")
    @ResponseBody public ResponseEntity<Object> search(
            @RequestParam(value = "name",defaultValue = "") String name,
            @RequestParam(value = "gender",defaultValue = "") String gender,
            @RequestParam(value = "work",defaultValue = "") String workStr,
            @RequestParam(value = "school",defaultValue = "") String schoolStr,
            @RequestParam(value = "size",defaultValue = "10") Integer size,
            @RequestParam(value = "from",defaultValue = "0") Integer from) {

        try {
            if(name.length()==0)
                return new ResponseEntity<>(
                        StatusCodeReturn.StatusCode("searchFB","Name should be provided","400").toString(),
                        HttpStatus.BAD_REQUEST);
            System.out.println(name + "," + gender + "," + workStr + "," + schoolStr);
            JSONObject result = FacebookSearcher.getSearchResult(name, gender, workStr, schoolStr,size,from);
            return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(StatusCodeReturn.StatusCode("searchFB","Error","404"),HttpStatus.NOT_FOUND);
    }

    @PutMapping("/facebook")
    @ResponseBody public ResponseEntity<Object> set(@RequestParam Map<String, String> values){
            System.out.println(values.toString());
            if(!values.containsKey("name"))
                return new ResponseEntity<>(
                        StatusCodeReturn.StatusCode("set","Name should be provided","400"),
                        HttpStatus.BAD_REQUEST);


            String name = values.get("name");

            String gender = "";
            if(values.containsKey("gender"))
                gender = values.get("gender");

            String schoolStr = "";
            if(values.containsKey("school"))
                schoolStr = values.get("school");

            String workStr = "";
            if(values.containsKey("work"))
                schoolStr = values.get("work");

            String link = "";
            if(values.containsKey("link"))
                link = values.get("link");


            String response = FacebookSearcher.putNewPerson(name, gender, schoolStr, workStr, link);
            System.out.println(response);
            return new ResponseEntity<>(
                    StatusCodeReturn.StatusCode("put profile", response, "200"),
                    HttpStatus.OK);
    }

}
