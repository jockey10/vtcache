# vtcache

Provides an API and in-memory database for storing and retrieving VMware tags and facts. `vtcache` is specifically designed to store and retrieve tags and facts returned by the Ansible [vmware_tags_facts](https://docs.ansible.com/ansible/devel/modules/vmware_tag_facts_module.html) module.

## design

`vtcache` is a Spring Boot application and uses an in-memory H2 database for caching tags and facts. This allows for rapid store and retrieval, and is flushed when the application is restarted. 

`vtcache` provides two API endpoints for interacting with tags:
- '/api/tags/store'(POST): deletes all tags currently present in the cache, and stores the JSON from the POST request as the new tags
- '/api/tags/retrieve'(GET): returns all tags currently in the cache as a JSON array

`vtcache` uses Spring Boot's embedded Tomcat web server, which coupled with the H2 in-memory database means it can be deployed as a fat-jar anywhere Java is running.

The application can be made more persistent by simply using a Docker container database, and updating the `application.properties` to use the new database parameters. You may also need to update dependencies.

## package

Maven can be used to create a fat-jar:

```
mvn package
...
...
[INFO] --- maven-jar-plugin:3.0.2:jar (default-jar) @ vtcache ---
[INFO] Building jar: /IdeaProjects/vtcache/target/vtcache-1.0-RELEASE.jar
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.0.3.RELEASE:repackage (default) @ vtcache ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 13.834 s
[INFO] Finished at: 2018-06-23T10:15:09+10:00
[INFO] Final Memory: 40M/326M
[INFO] ------------------------------------------------------------------------
```

## deployment

### requirements

`vtcache` only requires Java 8+ to be installed and configured. It has been tested on Fedora with the `java-1.8.0-openjdk` package.

### maven

You can test the latest `vtcache` functionality with maven:

```
export VTCACHE_PORT=9999
mvn spring-boot:run
```

### systemd

The application source comes with a systemd unit file, which you can use to control `vtcache` as a service:

```
mkdir /opt/vtcache
cp vtcache-1.0-RELEASE.jar /opt/vtcache/vtcache.jar

cp vtcache.service /usr/lib/systemd/system/
systemctl daemon-reload
systemctl start vtcache
```
You can now verify the application is started:
```
journalctl -fu vtcache

May 26 18:41:58 media.aliens systemd[1]: Started VMware Tags Cache.
May 26 18:41:59 media.aliens java[3281]:   .   ____          _            __ _ _
May 26 18:41:59 media.aliens java[3281]:  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
May 26 18:41:59 media.aliens java[3281]: ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
May 26 18:41:59 media.aliens java[3281]:  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
May 26 18:41:59 media.aliens java[3281]:   '  |____| .__|_| |_|_| |_\__, | / / / /
May 26 18:41:59 media.aliens java[3281]:  =========|_|==============|___/=/_/_/_/
...
```

## interact

### store tags

You can use the '/api/tags/store' endpoint to store tags from the 'vmware_tags_facts' output using an Ansible `to_json` filter. An example playbook is provided:
```
---
- name: vtcache json test
  hosts: localhost
  become: no
  gather_facts: no

  tasks:
    - set_fact:
        vtcache_tags: {'Sample_Tag_0002': {'tag_category_id': 'urn:vmomi:InventoryServiceCategory:6de17f28-7694-43ec-a783-d09c141819ae:GLOBAL', 'tag_description': 'Sample Description', 'tag_id': 'urn:vmomi:InventoryServiceTag:a141f212-0f82-4f05-8eb3-c49647c904c5:GLOBAL', 'tag_used_by': []}, 'ubuntu_machines': {'tag_category_id': 'urn:vmomi:InventoryServiceCategory:89573410-29b4-4cac-87a4-127c084f3d50:GLOBAL', 'tag_description': '', 'tag_id': 'urn:vmomi:InventoryServiceTag:7f3516d5-a750-4cb9-8610-6747eb39965d:GLOBAL', 'tag_used_by': []}, 'fedora_machines': {'tag_category_id': 'urn:vmomi:InventoryServiceCategory:baa90bae-951b-4e87-af8c-be681a1ba30c:GLOBAL', 'tag_description': '', 'tag_id': 'urn:vmomi:InventoryServiceTag:7d27d182-3ecd-4200-9d72-410cc6398a8a:GLOBAL', 'tag_used_by': []}}

    - debug:
        msg: '{{ vtcache_tags | to_json }}'

    - uri:
        url: http://localhost:9999/api/tags/store
        method: POST
        body: '{{ vtcache_tags | to_json }}'
        return_content: yes
        headers: { "Content-Type" : "application/json" }

```

### retrieve tags

The '/api/tags/retrieve' endpoint can be used to retrieve tags:

```
curl http://localhost:9999/api/tags/retrieve
[{"name":"Sample_Tag_0002","tag_category_id":"urn:vmomi:InventoryServiceCategory:6de17f28-7694-43ec-a783-d09c141819ae:GLOBAL","tag_description":"Sample Description","tag_id":"urn:vmomi:InventoryServiceTag:a141f212-0f82-4f05-8eb3-c49647c904c5:GLOBAL"},{"name":"ubuntu_machines","tag_category_id":"urn:vmomi:InventoryServiceCategory:89573410-29b4-4cac-87a4-127c084f3d50:GLOBAL","tag_description":"","tag_id":"urn:vmomi:InventoryServiceTag:7f3516d5-a750-4cb9-8610-6747eb39965d:GLOBAL"},{"name":"fedora_machines","tag_category_id":"urn:vmomi:InventoryServiceCategory:baa90bae-951b-4e87-af8c-be681a1ba30c:GLOBAL","tag_description":"","tag_id":"urn:vmomi:InventoryServiceTag:7d27d182-3ecd-4200-9d72-410cc6398a8a:GLOBAL"}]
```

### parse tags

Ruby can be used to easily parse tag details from a 'retrieve' request:

```
require 'rest-client'
require 'json'

response = RestClient.get('http://localhost:9999/api/tags/retrieve')
tags = JSON.parse(response)

tags.each {|tag| puts "#{tag["name"]} : #{tag["tag_description"]}"}
```
