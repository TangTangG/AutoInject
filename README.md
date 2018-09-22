# AutoInject
This a project that can help you build your manager.<br>
Especially for class that need to be controlled under different packages.<br>
Like the [Test(module)](.src/main/java/com/todo/autoinject).<br>

**package-a**<br>
&nbsp;&nbsp;&nbsp;&nbsp;**CollectionA.java**<br>
**package-b**<br>
&nbsp;&nbsp;&nbsp;&nbsp;**CollectionB.java**<br>
**CollectionC.java**<br>
**CollectionIface.java**<br>
**CollectionManager.java**<br>

Sometimes we want to holder all collection in CollectionManager.class,
but Collection in different package.<br>
If in same package,we can use java reflect to load them.<br>
In different package,now you can do like this.<br>
## First Step
Add CollectionRegister annotation to the class.
```java
@CollectionRegister(type = "c",target = CollectionIface.class)
public class CollectionC implements CollectionIface {
    @Override
    public void inject(Object[] args) {

    }
}
```
## Second Step
Add Collector annotation to the manager field.
```java
@Collector(CollectionIface.class)
Map<String,CollectionIface> collections = new HashMap<>(16);
```
## Final Step
When the (Manager)Class be initialed,do like this:
```java
AutoCollect.begin(new CollectionManager());
```
