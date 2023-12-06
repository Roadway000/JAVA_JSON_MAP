package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonElement;

import java.io.*;

import lombok.Data;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Data
public class TestJson {
    public static void main(String[] args) throws Exception {
        TestJson testJ = new TestJson();
        // Завдання 1
        // 1. створення нового об'єкта в https://jsonplaceholder.typicode.com/users.
        testJ.postJsonPlaceHolder(testJ.newUserToJson(null), "POST", "");
        // 2. оновлення об'єкту в https://jsonplaceholder.typicode.com/users
        testJ.postJsonPlaceHolder(testJ.newUserToJson(testJ.getUserById(10)), "PUT", "/10");
        // 3. видалення об'єкта з https://jsonplaceholder.typicode.com/users
        testJ.deleteJsonPlaceHolder(testJ.newUserToJson(testJ.getUserById(10)), "/10");
        // System.out.println(testJ.fileJsonToString());;
        // Лист усіх записей Users з файлу
        // for (User u: testJ.getAllUsersByJsonFile()) System.out.println(u);
        // 4. отримання інформації про всіх користувачів https://jsonplaceholder.typicode.com/users
        for (User u: testJ.getUsersByUrl()) System.out.println(u);
        // 5. отримання інформації про користувача за id https://jsonplaceholder.typicode.com/users/{id}
        System.out.println("getUserById(1) " + testJ.getUserById(1));
        // 6. отримання інформації про користувача за username - https://jsonplaceholder.typicode.com/users?username={username}
        System.out.println("getUserByUserName('Moriah.Stanton') " + testJ.getUserByUserName("Moriah.Stanton"));
        // Завдання 2 Доповніть програму методом, що буде виводити всі коментарі до останнього поста певного користувача і записувати їх у файл.
        testJ.getLastPostIdByUserId(10);
        // Завдання 3 Доповніть програму методом, що буде виводити всі відкриті задачі для користувача з ідентифікатором X.
        // https://jsonplaceholder.typicode.com/users/1/todos. Відкритими вважаються всі задачі, у яких completed = false
        testJ.getTodoByUserId(10);
    }
    public static void postJsonPlaceHolder(JSONObject jsonObj, String requestMethod, String requestSufix) throws IOException {
        String post_params = jsonObj.toString();
        System.out.println(post_params);
        URL url = new URL("https://jsonplaceholder.typicode.com/users"+requestSufix);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(requestMethod);
        conn.setRequestProperty("userId", "a1bcdefgh");
        conn.setRequestProperty("Content-Type", "application/json");

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(post_params.getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        System.out.println(requestMethod+" Response Code : " + responseCode + " Response Message : " + conn.getResponseMessage());

        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();
            // print result
            System.out.println(response.toString());
        } else {
            System.out.println(requestMethod+" NOT WORKED");
        }
    }
    public void deleteJsonPlaceHolder(JSONObject jsonObj, String requestSufix) {
        try {
            URL url = new URL("https://jsonplaceholder.typicode.com/users"+requestSufix);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            int responseCode = conn.getResponseCode();
            System.out.println(" Response Code : " + responseCode + " Response Message : " + conn.getResponseMessage());
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(" Resource deleted successfully");
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                System.out.println(" Resource not found");
            } else {
                System.out.println(" NOT WORKED");
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void userPost(JSONObject obj) throws Exception {
        URL url = new URL(" https://jsonplaceholder.typicode.com/users");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(String.valueOf(obj));
            out.close();
            conn.getInputStream();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public JSONObject newUserToJson(User user) {
        if (user == null)
            user = addUser();
        //SampleObject mSampleObject = new SampleObject();
        String jsonString = new Gson().toJson(user);
        JSONObject userAsJsonObject = new JSONObject(jsonString);
        return userAsJsonObject;
    }
    public User addUser() {
        User lastUser = getLastUser();
        System.out.println(lastUser);
        User user = new User();
        user.setId(lastUser.getId()+1);
        user.setName("type Name");
        user.setUsername("type User Name");
        user.setEmail("type user_emai@com");
        user.setAddress(new Address("type street", "type suit", "type city", "type zipcode 00000-0000", new Geo(0.00, 0.00)));
        user.setPhone("type phone number");
        user.setWebsite("type website");
        user.setCompany(new Company("type company name", "type catchPhrase", "type bs"));
        return user;
    }
    public User getLastUser() {
        List<User> list = getUsersByUrl();
        User user = list
                .stream()
                .max(Comparator.comparing(User::getId))
                .orElseThrow(NoSuchElementException::new);
        return user;
    }
    public List<User> getUsersByUrl() {
        String url = "https://jsonplaceholder.typicode.com/users";
        String jsonString = "";
        try {
            jsonString = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .get()
                    .body()
                    .text();

        } catch (IOException e) {
            System.out.println("error while users request");

        }
        Type type = TypeToken
                .getParameterized(List.class, User.class)
                .getType();
        List<User> list = new Gson().fromJson(jsonString, type);
        return list;
    }
    public User getUserById(int userId) {
        List<User> list = getUsersByUrl();
            list = list.stream()
                    .filter(c -> c.getId() == userId)
                    .collect(Collectors.toList());
            return (User) list.get(0);
    }
    public User getUserByUserName(String userName) {
        List<User> list = getUsersByUrl();
        list = list.stream()
                .filter(c -> c.getUsername().startsWith(userName))
                .collect(Collectors.toList());
        if (list.size()>0)
            return (User) list.get(0);
        else
            return (User) null;
    }

    public User[] getAllUsersByJsonFile() {
        Gson gson = new Gson();
        User[] users = null;
        try {
            users = gson.fromJson(new FileReader("src/staff.json"), User[].class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
    public String fileJsonToString() {
        Gson gson = new Gson();
        JsonElement json;
        {
            try {
                json = gson.fromJson(new FileReader("src/staff.json"), JsonElement.class);
            } catch (FileNotFoundException e) {
              throw new RuntimeException(e);
            }
        }
        return gson.toJson(json);
    }
    // P O S T ---------------------------------------------------------------------------------------------------------
    public void getLastPostIdByUserId(int userId) {
        String url = String.format("https://jsonplaceholder.typicode.com/users/%d/posts",userId);
        String jsonString = "";
        try {
            jsonString = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .get()
                    .body()
                    .text();
        } catch (IOException e) {
            System.out.println("error while posts request");

        }
        Type type = TypeToken
                .getParameterized(List.class, Post.class)
                .getType();
        List<Post> list = new Gson().fromJson(jsonString, type);
        Post post = list
                .stream()
                .max(Comparator.comparing(Post::getId))
                .orElseThrow(NoSuchElementException::new);
        if (post != null)
            getCommentByPostId(userId, post.getId());
    }
    public void getCommentByPostId(int userId, int postId) {
        String url = String.format("https://jsonplaceholder.typicode.com/posts/%d/comments",postId);
        String jsonString = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonString = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .get()
                    .body()
                    .text();
        } catch (IOException e) {
            System.out.println("error while comments request");
        }
        Type type = TypeToken
                .getParameterized(List.class, Comment.class)
                .getType();
        List<Comment> list = new Gson().fromJson(jsonString, type);
        if (list.size()>0) {
            String fileName = String.format("user-%d-post-%d-comments.json",userId,postId);
            writeCommentsToFile(jsonString, fileName);
            for (Comment comment : list)
                System.out.println(String.format("id: %s name: %s email: %s comment: %s", comment.getId(), comment.getName(), comment.getEmail(), comment.getBody().replaceAll("\n", " ")));
        }
    }
    // T O D O ---------------------------------------------------------------------------------------------------------
    public void getTodoByUserId(int userId) {
        String url = String.format("https://jsonplaceholder.typicode.com/users/%d/todos",userId);
        String jsonString = "";
        try {
            jsonString = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .get()
                    .body()
                    .text();
        } catch (IOException e) {
            System.out.println("error while tasks request");

        }
        Type type = TypeToken
                .getParameterized(List.class, Todo.class)
                .getType();
        List<Todo> list = new Gson().fromJson(jsonString, type);
        list = list.stream()
                .filter(c -> c.isCompleted()==false)
                .collect(Collectors.toList());

        if (list != null)
            for (Todo todo : list)
                System.out.println(String.format("userId: %d id: %d title: %s completed: %s",userId, todo.getId(), todo.getTitle(), todo.completed));
    }

    public static void writeCommentsToFile(String data, String fileName) {
        try(FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(data);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
@Data
class User {
    private int id;
    private String name; // "Ervin Howell"
    private String username; // "Antonette"
    private String email; // "Shanna@melissa.tv"
    private Address address;
    private String phone; // "010-692-6593 x09125"
    private String website; // "anastasia.net"
    private Company company;

    public User() {}
    public User(int id, String name, String username, String email, Address address, String phone, String website, Company company) {
        this.id = this.id;
        this.name = this.name;
        this.username = this.username;
        this.email = this.email;
        this.address = this.address;
        this.phone = this.phone;
        this.website = this.website;
        this.company = this.company;
    }
}
@Data
class Geo {
    private Double lat; // "-43.9509"
    private Double lng; // "-34.4618"

    public Geo(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
@Data
class Address {
    private String street; // "Victor Plains"
    private String suite; // "Suite 879"
    private String city; // "Wisokyburgh"
    private String zipcode; // "90566-7771"
    private Geo geo;

    public Address(String street, String suite, String city, String zipcode, Geo geo) {
        this.street = street;
        this.suite = suite;
        this.city = city;
        this.zipcode = zipcode;
        this.geo = geo;
    }
}
@Data
class Company {
    private String name; // "Deckow-Crist"
    private String catchPhrase; // "Proactive didactic contingency"
    private String bs; // "synergize scalable supply-chains"
    public Company(String name, String catchPhrase, String bs) {
        this.name = name;
        this.catchPhrase = catchPhrase;
        this.bs = bs;
    }
}
@Data
class Post{
    private int userId;
    private int id;
    private List<Comment> listComment;
    private String title;
    private String body;

    public Post(int userId, int id, List<Comment> listComments, String title, String body) {
        this.userId = userId;
        this.id = id;
        this.listComment = listComment;
        this.title = title;
        this.body = body;
    }
}
@Data
class Comment{
    private int postId;
    private int id;
    private String name;
    private String email;
    private String body;

    public Comment(int postId, int id, String name, String email, String body) {
        this.postId = postId;
        this.id = id;
        this.name = name;
        this.email = email;
        this.body = body;
    }
}
@Data
class Todo{
    public int userId;
    public int id;
    public String title;
    public boolean completed;
}