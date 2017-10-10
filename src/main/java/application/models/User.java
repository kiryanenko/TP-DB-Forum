package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class User {
    private Long id;

    // Имя пользователя (уникальное поле).
    // Данное поле допускает только латиницу, цифры и знак подчеркивания.
    // Сравнение имени регистронезависимо.
    // readOnly: true
    // example: j.sparrow
    @JsonProperty("nickname")
    private String nickname;

    // Полное имя пользователя.
    // x-isnullable: false
    // example: Captain Jack Sparrow
    @JsonProperty("fullname")
    private String fullname;

    // Почтовый адрес пользователя (уникальное поле).
    // x-isnullable: false
    // example: captaina@blackpearl.sea
    @JsonProperty("email")
    private String email;

    // Описание пользователя.
    // example: This is the day you will always remember as the day that you almost caught Captain Jack Sparrow!
    @JsonProperty("about")
    private String about;


    @JsonCreator
    public User(@JsonProperty("id") Long id,
                @JsonProperty("nickname") String nickname,
                @JsonProperty("fullname") String fullname,
                @JsonProperty("email") String email,
                @JsonProperty("about") String about) {
        this.id = id;
        this.nickname = nickname;
        this.fullname = fullname;
        this.email = email;
        this.about = about;
    }


    @JsonIgnore
    public Long getId() { return id; }
    public String getNickname() { return nickname;  }
    public String getFullname() { return fullname; }
    public String getEmail() { return email; }
    public String getAbout() { return about; }

    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public void setEmail(String email) { this.email = email; }
}
