# Unofficial Tile API for Wear OS

In early May 2019, Wear OS added *Tiles*, a widget-like UI for viewing snippets of app data alongside your watch face. Unfortunately, they didn't relase an API at the same time - meaning Tiles were limited to just Google (and select partners). As an Android dev with a long history of widget work, I wanted in on the fun.

So, I extracted the relevant code bits from the Wear OS app, and extrapolated the API. Hey presto, [it works](<https://youtu.be/Wm8eitGBKhw)!

NOTE: This is **very much** a work in progress, and there are still plenty of rough edges. I'll be updating this repo as work progresses.

NOTE 2: Although the API detailed below is undeniably unofficial, it does use the official Wear system-level hooks to create and update tiles on the watch. They're real tiles in the end.

## Installation
*UnofficialTileAPI* is a standard Android library, so just grab the source from this site (Download link at left) and include it in your project like you would any other.

## Use
Turns out Tiles are widget-like in more than just in appearance; they're based on `RemoteViews`, same as an AppWidget on an Android phone. So in order to provide a Tile, you'll need to build off that framework.

Once you have that in place, you'll need to extend `TileProviderService` and override `onTileUpdate` (more on that in a minute). When your `RemoteViews` are ready for display, you'll need to call the superclass' `sendData` method. 

There are also two other methods you can override, `onTileFocus` and `onTileBlur`. These work pretty much as you'd expect (though perhaps not exactly): `onTileFocus` gets called when the user swipes from their watch face onto the Tiles pane, and `onTileBlur` when they leave the pane. Note that these methods seem to be called for the pane as a whole, **not** your specific tile.

And they both get passed an integer parameter which I think is a consistent *tile ID*. These seem to get assigned sequentially when Tiles are added, remain consistent throughout the tile's lifetime, and don't get reused. [Again, if you've ever done an AppWidget, this is exactly like the `appWidgetId` they use.]

Note that you'll need this tile ID as the first parameter when calling `TileProviderService.sendData`, so it's a good idea to keep it around in a field.

Then there's the `onTileUpdate` method... It's a bit odd. It seems to be called **once** when your Tile is first added (or at boot), with your ID as its parameter. Thereafter, it'll be called occasionally with large negative numbers as a parameter; these obviously aren't your tile ID, but they aren't consistent either, so I'm not sure yet what they're about. I've seen official Tile code that handles parameter values < -2 differently, but haven't yet puzzled out the specifics.

#### Summary

My current analysis of how to implement a Tile:

 - Do any one-time initialization when `onTileUpdate` is called with a parameter value > -2. This seems to only happen when your tile is first added, or when the device boots.
 
 - If you have any per-process init, you can put it in the standard `Service.onCreate` method; `onTileUpdate` doesn't get called again if your process is restarted without a reboot.
 
 - Thereafter, use `onTileFocus` and `onTileBlur` for work like starting and stopping tile updates.

## Code
Here's a simple Tile you can use as a template, updating its data once per second while visible:

```kotlin
package my.packagename

import android.util.Log
import android.widget.RemoteViews

import com.google.android.clockwork.tiles.`TileData$Builder`
import com.google.android.clockwork.tiles.TileProviderService
import kotlinx.coroutines.*

import java.util.Date

class MyTileProviderService : TileProviderService() {

    private var id: Int = 0
    private var updateJob: Job? = null

    override fun onTileFocus(tileId: Int) {
        Log.d(TAG, "onTileFocus() called with: tileId = [$tileId]")
        id = tileId
        
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                sendRemoteViews()
                delay(1000)
            }
        }
    }

    override fun onTileBlur(tileId: Int) {
        Log.d(TAG, "onTileBlur() called with: var1 = [$tileId]")
        updateJob?.cancel()
    }

    override fun onTileUpdate(tileId: Int) {
        Log.d(TAG, "onTileUpdate() called with: tileId = [$tileId]")
        if (tileId == id) {
            sendRemoteViews()
        }
    }

    private fun sendRemoteViews() {
        Log.d(TAG, "sendRemoteViews")
        val remoteViews = RemoteViews(this.packageName, R.layout.tile)
        // Update tile UI here
        sendData(id, TileData.Builder().setRemoteViews(remoteViews).build())
    }

    companion object {
        private const val TAG = "MyTileProviderService"
    }
}
```

And of course, you'll also need it in your manifest:

```xml
<service
    android:label="@string/tile_name"
    android:icon="@drawable/ic_launcher"
    android:name=".MyTileProviderService"
    android:permission="com.google.android.wearable.permission.BIND_TILE_PROVIDER"
    android:exported="true"
    >
    <intent-filter>
        <action android:name="com.google.android.clockwork.ACTION_TILE_UPDATE_REQUEST" />
    </intent-filter>
</service>
```
## Disclaimers

 - There's no guarantee that this code won't break at any moment. I'm still figuring this stuff out. 
 - Given it's not a published API, Google may break it from their side.
 - There's **certainly** no guarantee that it will be compatible with any Tile API that Google may eventually release.
 - And just in case it wasn't clear, I have no affiliation with Google Inc

## Stuff to ponder

What are those funny negative inputs to `onTileUpdate`?

---
Given Tiles share so much foundation with AppWidgets, could we hope to see [collection-based](https://developer.android.com/guide/topics/appwidgets#collections) Tiles someday?

---
While rummaging around in the source, I found reference to `MultipleTiles`, in a package named `googledata.experiments.mobile.wear.features`. Now **that's** tantalizing! I could certainly see good uses for an arbitrary number of tiles from a singe app; perhaps this is a hint that such support is in the works?

---
