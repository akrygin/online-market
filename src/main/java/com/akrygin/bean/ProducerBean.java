package com.akrygin.bean;

import java.util.Objects;

public class ProducerBean {
    private String id;
    private String name;
    private String URL;

    public ProducerBean(String name, String url, String id) {
        this.name = name;
        this.URL = url;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProducerBean that = (ProducerBean) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(URL, that.URL);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, URL);
    }
}
