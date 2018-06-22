package org.rock.vtcache.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@JsonIgnoreProperties(value={ "tag_used_by" }, allowGetters = true)
public class Tag {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String name;
    private String tag_category_id;
    private String tag_description;
    private String tag_id;

    //JPA requires that a default constructor exists
    //for entities
    protected Tag() {}

    public Tag(String name,
               String tag_category_id,
               String tag_description,
               String tag_id) {
        this.name = name;
        this.tag_category_id = tag_category_id;
        this.tag_description = tag_description;
        this.tag_id = tag_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag_category_id() {
        return tag_category_id;
    }

    public void setTag_category_id(String tag_category_id) {
        this.tag_category_id = tag_category_id;
    }

    public String getTag_description() {
        return tag_description;
    }

    public void setTag_description(String tag_description) {
        this.tag_description = tag_description;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String toString() {

        return "<Tag:[Name: " + this.name + "],[tag_category: "+
                this.tag_category_id + "],[tag_description: "+
                this.tag_description + "],[tag_id:"+this.tag_id+"]";

    }
}
