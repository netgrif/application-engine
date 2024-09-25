# Shared roles
Shared roles or global roles are roles that are only created once and can be used and referenced across Petri nets.
To use a shared role in Petri nets first we must declare it. We can declare it as any other role with addition of ``global``
attribute set to ``true``:
```xml
<document xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
          xsi:noNamespaceSchemaLocation='https://petriflow.com/petriflow.schema.xsd'>
	<id>nae_1927</id>
	...
	<role global="true">
            <id>admin_global</id>
            <title>Global Administrator</title>
	</role>
	...
</document>
```
Then we can reference it as usual:
```xml
...
    <transition>
        <id>t1</id>
        <x>460</x>
        <y>180</y>
        <label>Global roles</label>
        <roleRef>
            <id>admin_global</id>
            <logic>
                <view>true</view>
                <perform>true</perform>
            </logic>
        </roleRef>
    </transition>
...
```
When importing a Petri net, the importer checks, whether the global role has already existed.
If not, the importer creates one. If there has been already one, the importer passes it to a the newly created net.