package com.netgrif.workflow.auth.domain;

import lombok.Data;

@Data
public class Author {

    private Long id;
    private String email;
    private String fullName;

    public Author() {
    }

    public Author(Long id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }

    public static Author createAnonymizedAuthor() {
        Author author = new Author();
        author.setId(-1L);
        author.setEmail("***");
        author.setFullName("***");
        return author;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
