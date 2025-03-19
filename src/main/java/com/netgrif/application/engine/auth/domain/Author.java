//package com.netgrif.application.engine.auth.domain;
//
//import lombok.Data;
//
//import java.io.Serializable;
//
//@Data
//public class Author implements Serializable {
//
//    private static final long serialVersionUID = 5167778985918909834L;
//
//    private String id;
//    private String email;
//    private String fullName;
//
//    public Author() {
//    }
//
//    public Author(String id, String email, String fullName) {
//        this.id = id;
//        this.email = email;
//        this.fullName = fullName;
//    }
//
//    public static Author createAnonymizedAuthor() {
//        Author author = new Author();
//        author.setId("");
//        author.setEmail("***");
//        author.setFullName("***");
//        return author;
//    }
//
//    @Override
//    public String toString() {
//        return "Author{" +
//                "id=" + id +
//                ", email='" + email + '\'' +
//                ", fullName='" + fullName + '\'' +
//                '}';
//    }
//
//    @Override
//    public Author clone() {
//        return new Author(this.id, this.email, this.fullName);
//    }
//}
