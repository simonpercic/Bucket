# Bucket 

Bucket is a disk cache library for Android. You can use it to cache any object that can be serialized to json.

[DiskLruCache by Jake Wharton](https://github.com/JakeWharton/DiskLruCache) is used as the underlying cache.

Supported operations:

- get
- put
- contains
- remove
- clear

Bucket contains synchronous, async and Rx methods for all operations.

## Usage

Add using Gradle:
```groovy
compile 'TODO'
```

#### Initialize
Create a **singleton** instance using a builder()
```java
// create a singleton instance using a builder()
int maxSizeBytes = 1024 * 1024;
Bucket bucket = Bucket.builder(context, maxSizeBytes).build();
```

You can also pass in a custom Gson instance, if you wish to do so:
```java
// create a singleton instance using a builder()
Gson gson = ...
Bucket.builder(context, maxSizeBytes).withGson(gson).build();
```

#### Get
```java
// sync
MyObject object = bucket.get("key", MyObject.class);

// async
bucket.getAsync("key", MyObject.class, new BucketGetCallback<MyObject>() {
            @Override public void onSuccess(MyObject object) {
                
            }

            @Override public void onFailure(Throwable throwable) {

            }
        });
        
// Rx
Observable<MyObject> observable = bucket.getRx("key", MyObject.class);
```

#### Put
```java
// sync
bucket.put("key", object);

// async
bucket.putAsync("key", object, new BucketCallback() {
            @Override public void onSuccess() {
                
            }

            @Override public void onFailure(Throwable throwable) {

            }
        });
        
// Rx
Observable<Boolean> observable = bucket.putRx("key", object);
```

#### Contains
```java
// sync
boolean contains = bucket.contains("key");

// async
bucket.containsAsync("key", new BucketGetCallback<Boolean>() {
            @Override public void onSuccess(Boolean contains) {
                
            }

            @Override public void onFailure(Throwable throwable) {

            }
        });
        
// Rx
Observable<Boolean> observable = bucket.containsRx("key");
```

#### Remove
```java
// sync
bucket.remove("key");

// async
bucket.removeAsync("key", new BucketCallback() {
            @Override public void onSuccess() {
                
            }

            @Override public void onFailure(Throwable throwable) {

            }
        });
        
// Rx
Observable<Boolean> observable = bucket.removeRx("key");
```

#### Clear
```java
// sync
bucket.clear();

// async
bucket.clearAsync(new BucketCallback() {
            @Override public void onSuccess() {
                
            }

            @Override public void onFailure(Throwable throwable) {

            }
        });
        
// Rx
Observable<Boolean> observable = bucket.clearRx();
```

### Generics / Collections support
Bucket fully supports Generics and Collections by passing a custom Type instance created through Gson:
```java
// generics
public class GenericObject<T> {
    T object;
    String value;
}

Type genericType = new TypeToken<GenericObject<MyObject>>(){}.getType();
GenericObject<MyObject> object = bucket.get("key", genericType);


// collections
Type collectionType = new TypeToken<List<MyObject>>() {}.getType();
List<MyObject> list = bucket.get("key", collectionType);
```

## Dependencies
Bucket depends on the following awesome open source projects:

- [DiskLruCache](https://github.com/JakeWharton/DiskLruCache)
- [Gson](https://github.com/google/gson)
- [SimpleDiskCache](https://github.com/fhucho/simple-disk-cache)
- [RxJava](https://github.com/ReactiveX/RxJava)
- [RxAndroid](https://github.com/ReactiveX/RxAndroid)

## Why use it?

- store any kind of object, as long as it is json-serializable
- relies on RxJava and RxAndroid schedulers for threading
- supports generics and collections
- unit and android test coverage
- checkstyle, findbugs, pmd and lint static code analysis checks

## Sample
Check out the [androidTest](bucket/src/androidTest/java/com/github/simonpercic/bucket) directory for practical examples.


## License

Open source, distributed under the MIT License. See [LICENSE](LICENSE) for details.
