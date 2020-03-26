## Custom Switch View  [![](https://jitpack.io/v/juan-goncalves/blloc-switch-view.svg)](https://jitpack.io/#juan-goncalves/blloc-switch-view) 
Custom lightweight switch for Android applications.

<p align="center">
<img src="recordings/toggle-example.gif" width="200" height="112" />
</p>

#### Features
- Draggable state indicator <br/><img src="recordings/drag-example.gif" width="200" height="112" /><br/>
- Dynamic color based on the position of the state indicator
- Automatically update the switch state after finishing a drag motion <br/><img src="recordings/snap-example.gif" width="200" height="112" /><br/>
- Customizable background color
- Allow toggling and setting the switch state programmatically


#### Usage

1. Add the JitPack repository to your build file
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
3. Add the library as a dependency in your project
`implementation 'me.juangoncalves.blloc.switchview:1.0.0'`

4. Use the `BllocSwitchView` class in your layouts
```
  ...
  
  <me.juangoncalves.switchview.BllocSwitchView  
	  android:id="@+id/bllocSwitch"  
	  android:layout_width="wrap_content"  
	  android:layout_height="wrap_content" />
	  
  ...
  ```
