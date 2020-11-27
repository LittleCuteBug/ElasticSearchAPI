package ElasticSearchFB;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import Utils.StringUtils;

public class FacebookSearcher {

  private static final RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
          RestClient.builder(
                  new HttpHost("localhost", 9200, "http")
          )
  );

  public static JSONObject convertResponse (SearchResponse response) {
    //System.out.println(response.toString());

    JSONObject result = new JSONObject();
    result.put("took",response.getTook().getStringRep());
    result.put("total",response.getHits().totalHits);
    result.put("code","searchFacebook");
    result.put("status",200);
    result.put("message","successed");
    JSONArray hits = new JSONArray();
    for (SearchHit hit : response.getHits().getHits())
    {
      JSONObject hitObject = new JSONObject();
      Map<String, Object> source = hit.getSourceAsMap();
      //hitObject.put("source", hit.getSourceAsMap());
      hitObject.put("name",source.get("name"));
      hitObject.put("gender",source.get("gender"));
      hitObject.put("link",source.get("link"));
      //hitObject.put("schoolAll",source.get("education"));

      if(source.containsKey("education")) {
        ArrayList<Object> objects = (ArrayList<Object>) source.get("education");
        JSONArray schools = new JSONArray();
        for (Object object : objects) {
          Map<String,Object> school = (Map<String, Object>) object;
          System.out.println("name: "+school.get("school"));
          Map<String,Object> name = (Map<String, Object>) school.get("school");
          System.out.println("ok: "+name.get("name"));
          schools.put(name.get("name"));
        }
        hitObject.put("school",schools);
      }
      //hitObject.put("workAll",source.get("work"));

      if(source.containsKey("work"))
      {
        ArrayList<Object> objects = (ArrayList<Object>) source.get("work");
        JSONArray works = new JSONArray();
        for (Object object : objects) {
          Map<String,Object> employer = (Map<String, Object>) object;
          System.out.println("name: "+employer.get("employer"));
          Map<String,Object> name = (Map<String, Object>) employer.get("employer");
          System.out.println("ok: "+name.get("name"));
          works.put(name.get("name"));
        }
        hitObject.put("work",works);
      }


      hits = hits.put(hitObject);
    }
    result = result.put("hits",hits);
    return result;


  }
  private static SearchResponse searchFacebook(QueryBuilder query, Integer size, Integer from){
    try{
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      searchSourceBuilder.query(query);
      searchSourceBuilder.size(size);
      searchSourceBuilder.from(from);
      searchSourceBuilder.fetchSource(new String[]{"name","link","gender","education.school.name","work.employer.name"}, null);
      SearchRequest searchRequest = new SearchRequest();
      searchRequest.indices("profile");
      searchRequest.source(searchSourceBuilder);
      return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    } catch (IOException e){
      e.printStackTrace();
    }
    return null;
}
  private static QueryBuilder builderSearchFacebook(String name, String gender, String worksStr, String schoolsStr){
    name = name.toLowerCase();
    String nameRemoveAccents = StringUtils.removeAccents(name);
    String[] nameSplit = name.split(" ");
    String firstName = nameSplit[0];
    String middleName = String.join(" ", Arrays.copyOfRange(nameSplit,1,nameSplit.length-1));
    String lastName = nameSplit[nameSplit.length-1];

    String[] nameRemoveAccentsSplit = nameRemoveAccents.split(" ");
    String firstNameRemoveAccents = nameRemoveAccentsSplit[0];
    String middleNameRemoveAccents = String.join(" ", Arrays.copyOfRange(nameRemoveAccentsSplit,1,nameRemoveAccentsSplit.length-1));
    String lastNameRemoveAccents = nameRemoveAccentsSplit[nameRemoveAccentsSplit.length-1];

    String[] nameCommons = {"nguyen", "tran", "le", "pham", "hoang", "vu", "bui", "do", "vo", "dang",
            "huynh", "ngo", "truong", "duong", "dinh", "ho", "doan", "dao", "trinh", "ha", "mai", "cao", "luong", "luu"};
    StringBuilder nameCommonsExclude = new StringBuilder();
    for( String nameCommon :nameCommons )
    {
      if(!nameRemoveAccents.contains(nameCommon))
      {
        //System.out.println(nameRemoveAccents + "not contains" + nameCommon);
        nameCommonsExclude.append(" ").append(nameCommon);
      }
    }

    worksStr = worksStr.toLowerCase();
    worksStr = StringUtils.removeAccents(worksStr);
    worksStr = StringUtils.removeStopWordsWork(worksStr);
    String[] works = worksStr.split(",");

    schoolsStr = schoolsStr.toLowerCase();
    schoolsStr = StringUtils.removeAccents(schoolsStr);
    schoolsStr = StringUtils.removeStopWordsSchool(schoolsStr);
    String[] schools = schoolsStr.split(",");


    BoolQueryBuilder query = QueryBuilders.boolQuery().must(
                    QueryBuilders.matchQuery("nameRemoveAccents", firstNameRemoveAccents + " " + lastNameRemoveAccents)
                            .operator(Operator.AND)
                            .boost((float) 1.0))
            .should(QueryBuilders.matchQuery("name",firstName).boost((float)1.75))
            .should(QueryBuilders.matchQuery("name",lastName))
            .should(QueryBuilders.matchQuery("name",middleName).operator(Operator.OR).boost((float) 1.5))
            .should(QueryBuilders.matchQuery("nameRemoveAccents",middleNameRemoveAccents).operator(Operator.OR))
            .mustNot(QueryBuilders.matchQuery("nameRemoveAccents",nameCommonsExclude.toString()).operator(Operator.OR))
            .should(QueryBuilders.matchQuery("gender",gender))
            .minimumShouldMatch(0);
    for (String work : works) {
      query.should(QueryBuilders.matchQuery("work.employer.name", work).operator(Operator.OR).boost((float) 2 / works.length));
    }

    for (String school : schools) {
      query.should(QueryBuilders.matchQuery("education.school.name", school).operator(Operator.OR).boost((float) 2/ schools.length));
    }
    return query;
  }
  public static JSONObject getSearchResult(String name, String gender, String workStr, String schoolStr, Integer size, Integer from){
    QueryBuilder query = builderSearchFacebook(name,gender,workStr,schoolStr);
    SearchResponse searchResponse = searchFacebook(query,size,from);
    assert searchResponse != null;
    return convertResponse(searchResponse);
  }

  public static String putNewPerson (String name, String gender, String schoolsStr, String worksStr, String link)
  {
    String nameRemoveAccents = StringUtils.removeAccents(name.toLowerCase());
    String[] works = worksStr.split(",");
    String[] schools = schoolsStr.split(",");

    JSONObject object = new JSONObject();
    object.put("name",name);
    object.put("nameRemoveAccents",nameRemoveAccents);
    object.put("gender",gender);
    object.put("link",link);

    System.out.println(name + nameRemoveAccents + gender);

    JSONArray worksArray = new JSONArray();
    for (String work : works)
    {
      JSONObject workObject = new JSONObject();
      JSONObject employerObject = new JSONObject();
      employerObject.put("name",StringUtils.removeAccents(work.toLowerCase()));
      workObject.put("employer",employerObject);
      worksArray.put(workObject);
    }
    object.put("work",worksArray);


    JSONArray schoolsArray = new JSONArray();
    for (String school : schools)
    {
      JSONObject schoolObject = new JSONObject();
      schoolObject.put("name",StringUtils.removeAccents(school.toLowerCase()));
      JSONObject educationObject = new JSONObject();
      educationObject.put("school",schoolObject);
      schoolsArray.put(educationObject);
    }
    object.put("education",schoolsArray);

    System.out.println(object);

    IndexRequest request = new IndexRequest();
    request.index("profile");
    request.type("_doc");
    request.source(object.toString(),XContentType.JSON);
    try {
      IndexResponse indexResponse = restHighLevelClient.index(request,RequestOptions.DEFAULT);
      System.out.println(indexResponse.getResult().toString());
      return indexResponse.getResult().toString();
    } catch (IOException ex)
    {
      ex.printStackTrace();
    }
    return "Failed";

  }

  public static void main(String[] args) {

    String name = "Nguyễn Quang Việt Thành";
    String gender = "male";
    String workStr = "";
    String schoolStr = "Học viện cảnh sát nhân dân";
    String link = "abc";
    Integer size = 10;
    Integer from = 100;
    System.out.println("getSearchResult\n"+getSearchResult(name,gender,workStr,schoolStr,size,from).toString());
    //putNewPerson(name,gender,schoolStr,workStr,link);
  }
}
