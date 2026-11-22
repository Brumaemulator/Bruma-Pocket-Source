// SPDX-License-Identifier: GPL-3.0-or-later
package com.brumastudio.pocket
import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.media.*
import android.os.Bundle
import android.view.*
import android.widget.*
import com.linkcore.emulator.*
import java.io.File
import java.util.concurrent.locks.LockSupport
class PlayerActivity:Activity(),SurfaceHolder.Callback {
 override fun attachBaseContext(base:android.content.Context){super.attachBaseContext(PocketLocale.attach(base))}
 companion object {
  fun closeForSaveImport(id:String){activeOwner?.takeIf{it.game.key==id}?.let{it.retire();it.finish()}}
  @Volatile private var owner=java.lang.ref.WeakReference<PlayerActivity>(null)
  private var activeOwner:PlayerActivity? get()=owner.get();set(value){owner=java.lang.ref.WeakReference(value)}
 }
 private var controllerConnected=false
 private var retired=false
 private fun retire(){
  if(retired)return
  stopGame();saveProgress();if(loaded)NativeCore.close();loaded=false
  RomAccess.release(rom);rom=null;retired=true
 }

 private lateinit var store:LibraryStore;private lateinit var game:LibraryGame;private lateinit var saveManager:SaveManager
 private lateinit var loading:TextView;private lateinit var surface:GameSurface;private lateinit var controls:PocketControls;private lateinit var gamepad:GamepadInput
 private lateinit var options:android.content.SharedPreferences;private var rom:File?=null
 @Volatile private var running=false;private var worker:Thread?=null;private var resumed=false;private var surfaceReady=false;private var userPaused=false;private var busy=true;private var loaded=false
 @Volatile private var fastForward=false;@Volatile private var fastMultiplier=2;@Volatile private var fastSound=false
 private var remapDialog:AlertDialog?=null
 private var resumeSession=false;private var sessionStart=0L;private var remap:Int?=null;private var shot:Bitmap?=null;private var destroyed=false
 override fun onCreate(b:Bundle?){PocketAppearance.apply(this);super.onCreate(b)
  store=LibraryStore(this);val found=store.read().find{it.key==intent.getStringExtra("id")};if(found==null){finish();return};game=found;activeOwner?.takeIf{it!==this}?.retire();activeOwner=this;saveManager=SaveManager(this);options=getSharedPreferences("pocket-options",0);fastSound=options.getBoolean("fastSound",false);resumeSession=intent.getBooleanExtra("resume",false)
  window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
  val frame=FrameLayout(this);frame.setBackgroundColor(Look.bg);surface=GameSurface(this);controls=PocketControls(this).apply{gba=game.system=="GBA";onFastForward={toggleFastForward()}};frame.addView(surface,FrameLayout.LayoutParams(-1,-1));frame.addView(controls,FrameLayout.LayoutParams(-1,-1))
  controls.addOnLayoutChangeListener{_,_,_,_,_,_,_,_,_->val region=GameLayout.viewport(controls.width,controls.height,resources.displayMetrics.density,if(game.system=="GBA")1.5f else 160f/144f);controls.screenRegion=region}
  window.statusBarColor=Look.bg;window.navigationBarColor=Look.bg
  if(android.os.Build.VERSION.SDK_INT>=28)window.attributes=window.attributes.apply{layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES}
  frame.setOnApplyWindowInsetsListener{_,i->i}
  val menu=Ui.button(this,getString(R.string.pause),false){if(controls.editing){controls.editing=false;controls.clearKeys();updateTouchVisibility();userPaused=false;startGame()}else pauseGame()};Ui.panel(menu,0xff171c29.toInt(),16);menu.setTextColor(0xffe2e5ed.toInt());frame.addView(menu,FrameLayout.LayoutParams(Ui.dp(this,92),Ui.dp(this,42)).apply{leftMargin=Ui.dp(this@PlayerActivity,12);topMargin=Ui.dp(this@PlayerActivity,12)})
  loading=Ui.text(this,getString(R.string.loading_game),16,Ui.sage).apply{gravity=Gravity.CENTER};frame.addView(loading,FrameLayout.LayoutParams(-1,Ui.dp(this,64),Gravity.CENTER));setContentView(frame);surface.holder.addCallback(this)
  gamepad=GamepadInput(this,{action->if(action==GamepadInput.PAUSE)pauseGame()else toggleFastForward()},{controls.clearKeys()},{connected->controllerConnected=connected;updateTouchVisibility()})
  Thread({try{val file=RomAccess.open(this,game);if(file.nameWithoutExtension!=game.key){RomAccess.release(file);error("Game source changed")};runOnUiThread{if(destroyed||retired||activeOwner!==this){RomAccess.release(file)}else{rom=file;busy=false;startGame()}}}catch(e:Exception){runOnUiThread{if(!destroyed&&!retired&&activeOwner===this){busy=false;loading.visibility=View.GONE;AlertDialog.Builder(this).setMessage(if(e.message=="Game source changed")R.string.source_changed else R.string.no_game).setPositiveButton(R.string.ok){_,_->finish()}.setOnCancelListener{finish()}.show()}}}},"Pocket-open").start()
 }
 private fun toggleFastForward(){if(busy||controls.editing)return;fastForward=!fastForward;controls.fastForwardActive=fastForward;toast(getString(if(!fastForward)R.string.normal_speed else if(fastMultiplier==4)R.string.quad_speed else R.string.double_speed))}
 private fun updateTouchVisibility(){controls.visibility=if(controls.editing||(options.getBoolean("showTouch",true)&&!(controllerConnected&&options.getBoolean("hideTouch",true))))View.VISIBLE else View.INVISIBLE;controls.clearKeys()}
 private fun rememberGame(){store.played(game.key,0);if(sessionStart==0L)sessionStart=android.os.SystemClock.elapsedRealtime()}
 private fun updateStatus(){}
 private fun toast(s:String){Toast.makeText(this,s,Toast.LENGTH_SHORT).show()}
 private fun showError(s:String){Ui.message(this,s)}
 private fun localizedError(e:Exception)=e.message
 // Flush only the game battery save; pause/back never creates an emulator snapshot.
 private fun saveProgress(){if(!loaded)return;val ok=NativeCore.save();if(!ok)toast(getString(R.string.save_failed));if(sessionStart!=0L){store.played(game.key,(android.os.SystemClock.elapsedRealtime()-sessionStart)/1000);sessionStart=0}}
 private fun pauseGame(){if(!::gamepad.isInitialized||busy)return;userPaused=true;stopGame();saveProgress()
  val choices=intArrayOf(R.string.continue_game,R.string.save_state,R.string.load_state,R.string.speed,R.string.controls,R.string.controller_map,R.string.screenshot,R.string.exit_game)
  AlertDialog.Builder(this).setTitle(game.title).setItems(choices.map{getString(it)}.toTypedArray()){_,i->when(i){0->{userPaused=false;startGame()};1->slots(false);2->slots(true);3->speed();4->touchSettings();5->mapping();6->capture();7->finish()}}.setOnCancelListener{userPaused=false;startGame()}.show()
 }
 private fun slots(load:Boolean){AlertDialog.Builder(this).setTitle(if(load)R.string.load_state else R.string.save_state).setItems((1..4).map{getString(R.string.slot,it)+(if(saveManager.state(game.key,it).exists())" ✓"else"")}.toTypedArray()){_,index->
  val file=saveManager.state(game.key,index+1)
  if(load){if(!file.exists()){toast(getString(R.string.empty_slot));pauseGame()}else{val ok=NativeCore.loadState(file.path);toast(getString(if(ok)R.string.saved else R.string.error));if(ok){surface.clearPending();userPaused=false;startGame()}else pauseGame()}}
  else{val ok=NativeCore.saveState(file.path);if(ok)NativeCore.saveState(saveManager.state(game.key,0).path);toast(getString(if(ok)R.string.saved else R.string.save_failed));pauseGame()}
 }.setNegativeButton(R.string.back){_,_->pauseGame()}.setOnCancelListener{pauseGame()}.show()}
 private fun speed(){val ids=intArrayOf(R.string.normal_speed,R.string.double_speed,R.string.quad_speed);AlertDialog.Builder(this).setTitle(R.string.speed).setView(Switch(this).apply{setText(R.string.fast_sound);isChecked=fastSound;setPadding(32,8,32,8);setOnCheckedChangeListener{_,enabled->fastSound=enabled;options.edit().putBoolean("fastSound",enabled).apply()}}).setItems(ids.map{getString(it)}.toTypedArray()){_,i->fastForward=i!=0;fastMultiplier=if(i==2)4 else 2;controls.fastForwardActive=fastForward;userPaused=false;startGame()}.setOnCancelListener{pauseGame()}.show()}
 private fun touchSettings(){val p=getSharedPreferences("pocket-controls",0);val box=Ui.column(this);box.setPadding(24,12,24,12)
  fun slider(id:Int,key:String,min:Float,max:Float,default:Float){box.addView(Ui.text(this,getString(id),14));val s=SeekBar(this);s.max=100;s.progress=((p.getFloat(key,default)-min)/(max-min)*100).toInt();s.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(v:SeekBar?,n:Int,user:Boolean){if(user){p.edit().putFloat(key,min+(max-min)*n/100).apply();controls.refresh()}};override fun onStartTrackingTouch(v:SeekBar?){};override fun onStopTrackingTouch(v:SeekBar?){}});box.addView(s)}
  fun toggle(id:Int,key:String,default:Boolean){val v=Switch(this);v.setText(id);v.isChecked=options.getBoolean(key,default);v.setOnCheckedChangeListener{_,enabled->options.edit().putBoolean(key,enabled).apply();updateTouchVisibility()};box.addView(v)}
  toggle(R.string.show_touch,"showTouch",true);toggle(R.string.hide_touch,"hideTouch",true)
  slider(R.string.control_size,"size",.65f,1.4f,1f);slider(R.string.opacity,"alpha",.2f,1f,.85f)
  val vibration=Switch(this);vibration.setText(R.string.vibration);vibration.isChecked=p.getBoolean("vibration",false);vibration.setOnCheckedChangeListener{_,b->p.edit().putBoolean("vibration",b).apply()};box.addView(vibration)
  box.addView(Ui.button(this,getString(R.string.reset_controls),false){controls.reset()})
  AlertDialog.Builder(this).setTitle(R.string.controls).setView(box).setPositiveButton(R.string.edit_controls){_,_->controls.visibility=View.VISIBLE;controls.editing=true;controls.clearKeys();toast(getString(R.string.edit_help))}.setNegativeButton(R.string.back){_,_->pauseGame()}.setOnCancelListener{pauseGame()}.show()
 }
 private fun mapping(){val names=arrayOf("A","B","SELECT","START","→","←","↑","↓","R","L",getString(R.string.speed),getString(R.string.reset_mapping));val masks=intArrayOf(1,2,4,8,16,32,64,128,256,512,GamepadInput.SPEED)
  AlertDialog.Builder(this).setTitle(R.string.controller_map).setItems(names){_,i->if(i==names.lastIndex){gamepad.reset();pauseGame()}else{remap=masks[i];remapDialog=AlertDialog.Builder(this).setMessage(getString(R.string.controller_press,names[i])).setNegativeButton(R.string.cancel){_,_->remap=null;pauseGame()}.setOnCancelListener{remap=null;pauseGame()}.create();remapDialog!!.setOnKeyListener{dialog,_,event->if(event.action==KeyEvent.ACTION_DOWN && GamepadInput.controller(event) && event.keyCode!=KeyEvent.KEYCODE_BACK){gamepad.bind(event,masks[i]);remap=null;dialog.dismiss();remapDialog=null;toast(getString(R.string.mapping_saved));pauseGame();true}else false};remapDialog!!.show()}}.setOnCancelListener{pauseGame()}.show()
 }
 private fun capture(){shot?.recycle();shot=surface.snapshot();if(shot==null){toast(getString(R.string.error));return};startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).setType("image/png").putExtra(Intent.EXTRA_TITLE,"Pocket-${game.key.take(10)}.png"),35)}
 override fun onActivityResult(r:Int,result:Int,data:Intent?){super.onActivityResult(r,result,data);if(r==35){try{if(result==RESULT_OK)data?.data?.let{u->contentResolver.openOutputStream(u)!!.use{check(shot!!.compress(Bitmap.CompressFormat.PNG,100,it))};toast(getString(R.string.saved))}}catch(e:Exception){toast(getString(R.string.error))}finally{shot?.recycle();shot=null};pauseGame()}}
 override fun dispatchKeyEvent(e:KeyEvent):Boolean{if(::gamepad.isInitialized){if(remap!=null&&e.action==KeyEvent.ACTION_DOWN&&GamepadInput.controller(e)){gamepad.bind(e,remap!!);remap=null;toast(getString(R.string.mapping_saved));pauseGame();return true};if(gamepad.key(e))return true};return super.dispatchKeyEvent(e)}
 override fun onGenericMotionEvent(e:MotionEvent)=if(::gamepad.isInitialized&&gamepad.motion(e))true else super.onGenericMotionEvent(e)
 override fun onResume(){super.onResume();if(retired){finish();return};resumed=true;if(::surface.isInitialized)startGame()}
 override fun onPause(){resumed=false;if(::game.isInitialized){stopGame();saveProgress()};super.onPause()}
 override fun onDestroy(){destroyed=true;remapDialog?.dismiss();remapDialog=null;if(::game.isInitialized){stopGame();if(loaded&&activeOwner===this)NativeCore.close();if(activeOwner===this)activeOwner=null;RomAccess.release(rom);rom=null;if(::gamepad.isInitialized)gamepad.close()};shot?.recycle();super.onDestroy()}
 override fun onBackPressed(){pauseGame()}
 override fun surfaceCreated(h:SurfaceHolder){surfaceReady=true;surface.setGameFrameRate();startGame()}
 override fun surfaceDestroyed(h:SurfaceHolder){surfaceReady=false;stopGame()}
 override fun surfaceChanged(h:SurfaceHolder,f:Int,w:Int,height:Int){surface.clearPending()}
    private fun startGame() {
        val file = rom ?: return
        if (retired || activeOwner!==this || !resumed || !surfaceReady || userPaused || running || busy) return
        if(sessionStart==0L)sessionStart=android.os.SystemClock.elapsedRealtime()
        running = true
        worker = Thread({
            var audio: AudioTrack? = null
            try {
                if (!loaded) {
                    val saves = File(filesDir, "saves").apply { mkdirs() }
                    val error = NativeCore.load(file.path, File(saves, game.key + ".sav").path)
                    check(error == null) { error ?: getString(R.string.no_game) }
                    loaded = true
                    if(resumeSession && saveManager.state(game.key,0).exists()) {
                        if(!NativeCore.loadState(saveManager.state(game.key,0).path))runOnUiThread{toast(getString(R.string.error))}
                    }
                    surface.fillScreen=options.getBoolean("stretch",false)
                    surface.integerScaling=false
                    runOnUiThread { if (!isDestroyed && !retired && activeOwner===this){loading.visibility=View.GONE;rememberGame()} }
                }
                val format = AudioFormat.Builder().setSampleRate(48000)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build()
                audio = AudioTrack.Builder().setAudioFormat(format)
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    .setBufferSizeInBytes(maxOf(16384, AudioTrack.getMinBufferSize(48000,
                        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT)))
                    .setTransferMode(AudioTrack.MODE_STREAM).build()
                audio.setVolume(options.getInt("volume",80)/100f)
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
                val videoSize=NativeCore.videoSize()
                val bitmap = Bitmap.createBitmap(videoSize ushr 16, videoSize and 65535, Bitmap.Config.ARGB_8888)
                val isGameBoy = bitmap.width == 160 && bitmap.height == 144
                val samples = ShortArray(4096)
                var audioStarted = false
                var previousSpeed=0
                var previousSound=false
                var deadline=System.nanoTime()
                val accelerated=ShortArray(4096)
                val resampler=SpeedAudio()
                val performanceStart = System.nanoTime()
                var nextSave=System.nanoTime()+5_000_000_000L
                var frames = 0
                while (running) {
                    val count = NativeCore.frame(controls.keys or gamepad.keys, bitmap, samples)
                    val generatedAt=System.nanoTime()
                    check(count >= 0) { getString(R.string.error) }
                    val speed=if(fastForward) fastMultiplier else 1
                    val audible=speed==1 || fastSound
                    if(speed!=previousSpeed || audible!=previousSound) {
                        // Only stop and discard queued audio when sound is actually muted.
                        // Changing 2x <-> 4x keeps the live track running; flushing it on every
                        // tap can leave AudioTrack rejecting the next blocking write.
                        if(audible!=previousSound) {
                            if(audible) {
                                audio.play();audioStarted=true
                            } else {
                                audio.pause();audio.flush();audioStarted=false
                            }
                        }
                        deadline=System.nanoTime()
                        resampler.reset();previousSpeed=speed;previousSound=audible
                    }
                    // AudioTrack must be playing before a blocking stream write starts.
                    if(audible && !audioStarted) {audio.play();audioStarted=true}
                    val audioCount=if(speed==1) count else resampler.convert(samples,count,speed,accelerated)
                    val pcm=if(speed==1) samples else accelerated
                    var offset=0
                    while(running && audible && offset<audioCount) {
                        val written=audio.write(pcm,offset,audioCount-offset,AudioTrack.WRITE_BLOCKING)
                        if(written<=0) android.util.Log.e("BrumaAudio","AudioTrack.write returned $written; state=${audio.state}, speed=${speed}x, samples=${audioCount-offset}")
                        check(written>0) { getString(R.string.error) };offset+=written
                    }
                    // GB/GBC frames are ready now; publish before waiting for the next tick.
                    if(isGameBoy && frames%speed==0) surface.drawFrame(bitmap,generatedAt)
                    // Pace every emulated frame, including the initial audio-buffer fill.
                    deadline+=16_742_706L/speed
                    val wait=deadline-System.nanoTime()
                    if(wait>0) LockSupport.parkNanos(wait)
                    else if(wait < -50_000_000L) deadline=System.nanoTime()
                    if(!isGameBoy && frames%speed==0) surface.drawFrame(bitmap,generatedAt)
                    if (frames > 0 && frames % 300 == 0) android.util.Log.i("BrumaPerf", "fps=" + (frames*1e9/(System.nanoTime()-performanceStart)) + " underruns=" + audio.underrunCount + " fill=" + surface.fillScreen + " speed=" + speed + " sound=" + audible)
                    frames++
                    val now=System.nanoTime()
                    if(now>=nextSave) {
                        if(!NativeCore.save()) error(getString(R.string.save_failed))
                        nextSave=System.nanoTime()+5_000_000_000L
                    }
                }
                bitmap.recycle()
            } catch (error: Exception) {
                android.util.Log.e("BrumaPerf", "Operation failed", error)
                runOnUiThread {
                    if (!isDestroyed && !retired && activeOwner===this) {
                        userPaused = true
                        updateStatus()
                        showError(localizedError(error) ?: getString(R.string.error))
                    }
                }
            } finally {
                audio?.pause()
                audio?.flush()
                audio?.release()
                NativeCore.save()
                running = false
            }
        }, "Pocket-mGBA").also { it.start() }
    }

    private fun stopGame() {
        running = false
        worker?.let { LockSupport.unpark(it); it.join() }
        worker = null
        surface.clearPending()
        controls.clearKeys();gamepad.clear()
    }


}
