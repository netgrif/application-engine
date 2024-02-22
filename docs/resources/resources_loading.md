# Resource Loading

If you want to load resources, which will not be in JAR(for example big files), you can use our resource loader.
ResourceLoader return InputStreamResource, from which you can use InputStream, and loading resources from directory **resource/** in root of the project.
The prefix for NetgrifResourceLoaser is 

```
resource://
```

For use you can use code like this in your runner:
```java
    @Autowired
    private ResourceLoader resourceLoader;

    @Value("resource://nameOfFile.txt")
    private Resource customResource;

    @Override
    void run(String... strings) throws Exception {
        loadResources("resource://nameOfFile.txt");
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

