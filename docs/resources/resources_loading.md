# Resource Loading

If you want to load resources, which are not included in JAR (for example large files), you can use the resource loader.
ResourceLoader returns an InputStreamResource. You can turn it into an InputStream and load resources from the directory **resource/**  in the working directory of the app.
The prefix for ExternalResourceLoader is 

```
resource:
```

For use you can use code like this in your runner:
```java
    @Autowired
    private ResourceLoader resourceLoader;

    @Value("resource:nameOfFile.txt")
    private Resource customResource;

    @Override
    void run(String... strings) throws Exception {
        loadResources("resource:nameOfFile.txt");
    }

    void loadResources(String resourceUrl) {
        var resource = resourceLoader.getResource(resourceUrl);
        var txt = new String(resource.getInputStream().readAllBytes());
        System.out.println("File content: " + txt);
    }

    void getCustomResource() throws IOException {
        var txt = new String(customResource.getInputStream().readAllBytes());
        System.out.println("Resource from property: " + txt);
    }
```

