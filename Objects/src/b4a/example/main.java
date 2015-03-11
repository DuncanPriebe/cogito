package b4a.example;

import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	private static final boolean fullScreen = true;
	private static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                anywheresoftware.b4a.keywords.Common.Log("Killing previous instance (main).");
				p.finish();
			}
		}
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        initializeProcessGlobals();		
        initializeGlobals();
        
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
		return true;
	}
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true)
				return true;
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
		this.setIntent(intent);
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public static boolean _resetflag = false;
public static boolean _alternatecontrol = false;
public static int _ruleset = 0;
public static boolean _soundenabled = false;
public static anywheresoftware.b4a.audio.SoundPoolWrapper _sp = null;
public static int _soundcardclick = 0;
public static int _soundendturn = 0;
public static int _soundendgame = 0;
public static int _soundnewgame = 0;
public static String _playerturn = "";
public static String _startingturn = "";
public static int _cardsplayedlastturn = 0;
public static int _cardsplayedthisturn = 0;
public static int _lastbattle = 0;
public static int _heartbattlewins = 0;
public static int _spadebattlewins = 0;
public static int _heartscore = 0;
public static int _spadescore = 0;
public static int[] _heartcardvalue = null;
public static int[] _spadecardvalue = null;
public static int _heartcardsplayed = 0;
public static int _spadecardsplayed = 0;
public anywheresoftware.b4a.objects.ImageViewWrapper _c1 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c2 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c3 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c4 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c5 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c6 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c7 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c8 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c9 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c10 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c11 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c12 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c13 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c14 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c15 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _c16 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper[] _imagecards = null;
public anywheresoftware.b4a.objects.LabelWrapper _b1 = null;
public anywheresoftware.b4a.objects.LabelWrapper _b2 = null;
public anywheresoftware.b4a.objects.LabelWrapper _b3 = null;
public anywheresoftware.b4a.objects.LabelWrapper _b4 = null;
public anywheresoftware.b4a.objects.LabelWrapper _b5 = null;
public anywheresoftware.b4a.objects.LabelWrapper _b6 = null;
public anywheresoftware.b4a.objects.LabelWrapper[] _labelbattle = null;
public anywheresoftware.b4a.objects.PanelWrapper _panelscoreboard = null;
public anywheresoftware.b4a.objects.LabelWrapper _labelheartscore = null;
public anywheresoftware.b4a.objects.LabelWrapper _labelspadescore = null;
public anywheresoftware.b4a.objects.ButtonWrapper _buttonendturn = null;
public static boolean _gameover = false;
public b4a.example.options _options = null;
public static String  _activity_create(boolean _firsttime) throws Exception{
int _i = 0;
 //BA.debugLineNum = 74;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 75;BA.debugLine="If FirstTime Then 'If the game was started, not resumed.";
if (_firsttime) { 
 //BA.debugLineNum = 76;BA.debugLine="SP.Initialize(3)";
_sp.Initialize((int)(3));
 //BA.debugLineNum = 77;BA.debugLine="SoundCardClick = SP.Load(File.DirAssets, \"click1.wav\")";
_soundcardclick = _sp.Load(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"click1.wav");
 //BA.debugLineNum = 78;BA.debugLine="SoundEndTurn = SP.Load(File.DirAssets, \"endturn.wav\")";
_soundendturn = _sp.Load(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"endturn.wav");
 //BA.debugLineNum = 79;BA.debugLine="SoundEndGame = SP.Load(File.DirAssets, \"endgame.wav\")";
_soundendgame = _sp.Load(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"endgame.wav");
 //BA.debugLineNum = 80;BA.debugLine="SoundNewGame = SP.Load(File.DirAssets, \"newgame.wav\")";
_soundnewgame = _sp.Load(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"newgame.wav");
 //BA.debugLineNum = 81;BA.debugLine="SoundEnabled = True";
_soundenabled = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 82;BA.debugLine="AlternateControl = True";
_alternatecontrol = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 83;BA.debugLine="RuleSet = 1";
_ruleset = (int)(1);
 //BA.debugLineNum = 84;BA.debugLine="StartingTurn = \"s\"";
mostCurrent._startingturn = "s";
 //BA.debugLineNum = 85;BA.debugLine="Activity.AddMenuItem(\"Options\", \"Menu\")";
mostCurrent._activity.AddMenuItem("Options","Menu");
 //BA.debugLineNum = 87;BA.debugLine="Activity.AddMenuItem(\"Help\", \"Menu\")";
mostCurrent._activity.AddMenuItem("Help","Menu");
 //BA.debugLineNum = 88;BA.debugLine="Activity.AddMenuItem(\"Exit\", \"Menu\")";
mostCurrent._activity.AddMenuItem("Exit","Menu");
 };
 //BA.debugLineNum = 91;BA.debugLine="Activity.LoadLayout(\"cogito_main\")";
mostCurrent._activity.LoadLayout("cogito_main",mostCurrent.activityBA);
 //BA.debugLineNum = 92;BA.debugLine="Activity.Color = Colors.RGB(1, 105, 43) 'Change background color.";
mostCurrent._activity.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(1),(int)(105),(int)(43)));
 //BA.debugLineNum = 94;BA.debugLine="ImageCards = Array As ImageView(C1, C2, C3, C4, C5, C6, C7, C8, C9, C10, C11, C12, C13, C14, C15, C16)";
mostCurrent._imagecards = new anywheresoftware.b4a.objects.ImageViewWrapper[]{mostCurrent._c1,mostCurrent._c2,mostCurrent._c3,mostCurrent._c4,mostCurrent._c5,mostCurrent._c6,mostCurrent._c7,mostCurrent._c8,mostCurrent._c9,mostCurrent._c10,mostCurrent._c11,mostCurrent._c12,mostCurrent._c13,mostCurrent._c14,mostCurrent._c15,mostCurrent._c16};
 //BA.debugLineNum = 95;BA.debugLine="LabelBattle = Array As Label(B1, B2, B3, B4, B5, B6)";
mostCurrent._labelbattle = new anywheresoftware.b4a.objects.LabelWrapper[]{mostCurrent._b1,mostCurrent._b2,mostCurrent._b3,mostCurrent._b4,mostCurrent._b5,mostCurrent._b6};
 //BA.debugLineNum = 97;BA.debugLine="For i = 1 To 5 'Add card imageviews and battle labels.";
{
final double step54 = 1;
final double limit54 = (int)(5);
for (_i = (int)(1); (step54 > 0 && _i <= limit54) || (step54 < 0 && _i >= limit54); _i += step54) {
 //BA.debugLineNum = 98;BA.debugLine="ImageCards(i).Initialize(\"ImageCard\")";
mostCurrent._imagecards[_i].Initialize(mostCurrent.activityBA,"ImageCard");
 //BA.debugLineNum = 99;BA.debugLine="Activity.AddView(ImageCards(i), (10%x * i) + 16.25%x, 20%y, 7.29%x, 15%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._imagecards[_i].getObject()),(int)((anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(10),mostCurrent.activityBA)*_i)+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(16.25),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(7.29),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(15),mostCurrent.activityBA));
 //BA.debugLineNum = 100;BA.debugLine="ImageCards(i).Tag = i";
mostCurrent._imagecards[_i].setTag((Object)(_i));
 //BA.debugLineNum = 101;BA.debugLine="ImageCards(i + 5).Initialize(\"ImageCard\")";
mostCurrent._imagecards[(int)(_i+5)].Initialize(mostCurrent.activityBA,"ImageCard");
 //BA.debugLineNum = 102;BA.debugLine="Activity.AddView(ImageCards(i + 5), (10%x * i) + 16.25%x, 45%y, 7.29%x, 15%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._imagecards[(int)(_i+5)].getObject()),(int)((anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(10),mostCurrent.activityBA)*_i)+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(16.25),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(45),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(7.29),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(15),mostCurrent.activityBA));
 //BA.debugLineNum = 103;BA.debugLine="ImageCards(i + 5).Tag = i + 5";
mostCurrent._imagecards[(int)(_i+5)].setTag((Object)(_i+5));
 //BA.debugLineNum = 104;BA.debugLine="ImageCards(i + 10).Initialize(\"ImageCard\")";
mostCurrent._imagecards[(int)(_i+10)].Initialize(mostCurrent.activityBA,"ImageCard");
 //BA.debugLineNum = 105;BA.debugLine="Activity.AddView(ImageCards(i + 10), (10%x * i) + 16.25%x, 65%y, 7.29%x, 15%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._imagecards[(int)(_i+10)].getObject()),(int)((anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(10),mostCurrent.activityBA)*_i)+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(16.25),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(65),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(7.29),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(15),mostCurrent.activityBA));
 //BA.debugLineNum = 106;BA.debugLine="ImageCards(i + 10).Tag = i + 10";
mostCurrent._imagecards[(int)(_i+10)].setTag((Object)(_i+10));
 //BA.debugLineNum = 108;BA.debugLine="LabelBattle(i).Initialize(\"LabelBattles\")";
mostCurrent._labelbattle[_i].Initialize(mostCurrent.activityBA,"LabelBattles");
 //BA.debugLineNum = 109;BA.debugLine="Activity.AddView(LabelBattle(i), (10%x * i) + 16.25%x, 37.5%y, 7.29%x, 5%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._labelbattle[_i].getObject()),(int)((anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(10),mostCurrent.activityBA)*_i)+anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(16.25),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(37.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(7.29),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(5),mostCurrent.activityBA));
 //BA.debugLineNum = 110;BA.debugLine="LabelBattle(i).Gravity = Gravity.CENTER_HORIZONTAL";
mostCurrent._labelbattle[_i].setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER_HORIZONTAL);
 //BA.debugLineNum = 111;BA.debugLine="LabelBattle(i).TextSize = 10";
mostCurrent._labelbattle[_i].setTextSize((float)(10));
 //BA.debugLineNum = 112;BA.debugLine="LabelBattle(i).TextColor = Colors.White";
mostCurrent._labelbattle[_i].setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 }
};
 //BA.debugLineNum = 115;BA.debugLine="PanelScoreBoard.Initialize(\"\") 'Add score panel and labels.";
mostCurrent._panelscoreboard.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 116;BA.debugLine="Activity.AddView(PanelScoreBoard, 30%x, 0, 40%x, 12.5%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._panelscoreboard.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(30),mostCurrent.activityBA),(int)(0),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(12.5),mostCurrent.activityBA));
 //BA.debugLineNum = 117;BA.debugLine="PanelScoreBoard.Color = Colors.RGB(1, 65, 23)";
mostCurrent._panelscoreboard.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int)(1),(int)(65),(int)(23)));
 //BA.debugLineNum = 118;BA.debugLine="LabelHeartScore.Initialize(\"\")";
mostCurrent._labelheartscore.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 119;BA.debugLine="Activity.AddView(LabelHeartScore, 35%x, 3.75%y, 12.5%x, 5%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._labelheartscore.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(35),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(3.75),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(12.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(5),mostCurrent.activityBA));
 //BA.debugLineNum = 120;BA.debugLine="LabelHeartScore.Text = \"Hearts: \" & HeartScore";
mostCurrent._labelheartscore.setText((Object)("Hearts: "+BA.NumberToString(_heartscore)));
 //BA.debugLineNum = 121;BA.debugLine="LabelHeartScore.TextSize = 10";
mostCurrent._labelheartscore.setTextSize((float)(10));
 //BA.debugLineNum = 122;BA.debugLine="LabelHeartScore.TextColor = Colors.White";
mostCurrent._labelheartscore.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 123;BA.debugLine="LabelSpadeScore.Initialize(\"\")";
mostCurrent._labelspadescore.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 124;BA.debugLine="Activity.AddView(LabelSpadeScore, 57%x, 3.75%y, 12.5%x, 5%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._labelspadescore.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(57),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(3.75),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(12.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(5),mostCurrent.activityBA));
 //BA.debugLineNum = 125;BA.debugLine="LabelSpadeScore.Text = \"Spades: \" & SpadeScore";
mostCurrent._labelspadescore.setText((Object)("Spades: "+BA.NumberToString(_spadescore)));
 //BA.debugLineNum = 126;BA.debugLine="LabelSpadeScore.TextSize = 10";
mostCurrent._labelspadescore.setTextSize((float)(10));
 //BA.debugLineNum = 127;BA.debugLine="LabelSpadeScore.TextColor = Colors.White";
mostCurrent._labelspadescore.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 129;BA.debugLine="ButtonEndTurn.Initialize(\"ButtonEndTurn\") 'Add the End Turn button.";
mostCurrent._buttonendturn.Initialize(mostCurrent.activityBA,"ButtonEndTurn");
 //BA.debugLineNum = 130;BA.debugLine="Activity.AddView(ButtonEndTurn, 40%x, 82.5%y, 20%x, 15%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._buttonendturn.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(82.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(15),mostCurrent.activityBA));
 //BA.debugLineNum = 131;BA.debugLine="ButtonEndTurn.Text = \"End Turn\"";
mostCurrent._buttonendturn.setText((Object)("End Turn"));
 //BA.debugLineNum = 135;BA.debugLine="NewGame";
_newgame();
 //BA.debugLineNum = 137;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 352;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 354;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 345;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 346;BA.debugLine="If ResetFlag = True Then 'Check if the game should be reset.";
if (_resetflag==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 347;BA.debugLine="If CardsPlayedThisTurn <> 0 OR CardsPlayedLastTurn <> 0 Then NewGame";
if (_cardsplayedthisturn!=0 || _cardsplayedlastturn!=0) { 
_newgame();};
 //BA.debugLineNum = 348;BA.debugLine="ResetFlag = False";
_resetflag = anywheresoftware.b4a.keywords.Common.False;
 };
 //BA.debugLineNum = 350;BA.debugLine="End Sub";
return "";
}
public static String  _battle() throws Exception{
int _i = 0;
 //BA.debugLineNum = 247;BA.debugLine="Sub Battle";
 //BA.debugLineNum = 248;BA.debugLine="For i = (LastBattle + 1) To 5";
{
final double step179 = 1;
final double limit179 = (int)(5);
for (_i = (int)((_lastbattle+1)); (step179 > 0 && _i <= limit179) || (step179 < 0 && _i >= limit179); _i += step179) {
 //BA.debugLineNum = 249;BA.debugLine="If HeartCardValue(i) > 0 AND SpadeCardValue(i) > 0 Then 'If both players have played a card";
if (_heartcardvalue[_i]>0 && _spadecardvalue[_i]>0) { 
 //BA.debugLineNum = 250;BA.debugLine="If HeartCardValue(i) = 1 AND SpadeCardValue(i) = 5	Then 'If Hearts played a two on an ace";
if (_heartcardvalue[_i]==1 && _spadecardvalue[_i]==5) { 
 //BA.debugLineNum = 251;BA.debugLine="LabelBattle(i).Text = \"Hearts\"";
mostCurrent._labelbattle[_i].setText((Object)("Hearts"));
 //BA.debugLineNum = 252;BA.debugLine="HeartBattleWins = HeartBattleWins + 1";
_heartbattlewins = (int)(_heartbattlewins+1);
 }else if(_spadecardvalue[_i]==1 && _heartcardvalue[_i]==5) { 
 //BA.debugLineNum = 254;BA.debugLine="LabelBattle(i).Text = \"Spades\"";
mostCurrent._labelbattle[_i].setText((Object)("Spades"));
 //BA.debugLineNum = 255;BA.debugLine="SpadeBattleWins = SpadeBattleWins + 1";
_spadebattlewins = (int)(_spadebattlewins+1);
 }else if(_heartcardvalue[_i]>_spadecardvalue[_i]) { 
 //BA.debugLineNum = 257;BA.debugLine="LabelBattle(i).Text = \"Hearts\"";
mostCurrent._labelbattle[_i].setText((Object)("Hearts"));
 //BA.debugLineNum = 258;BA.debugLine="HeartBattleWins = HeartBattleWins + 1";
_heartbattlewins = (int)(_heartbattlewins+1);
 }else if(_heartcardvalue[_i]<_spadecardvalue[_i]) { 
 //BA.debugLineNum = 260;BA.debugLine="LabelBattle(i).Text = \"Spades\"";
mostCurrent._labelbattle[_i].setText((Object)("Spades"));
 //BA.debugLineNum = 261;BA.debugLine="SpadeBattleWins = SpadeBattleWins + 1";
_spadebattlewins = (int)(_spadebattlewins+1);
 }else if(_heartcardvalue[_i]==_spadecardvalue[_i]) { 
 //BA.debugLineNum = 263;BA.debugLine="LabelBattle(i).Text = \"Tie\"";
mostCurrent._labelbattle[_i].setText((Object)("Tie"));
 };
 //BA.debugLineNum = 265;BA.debugLine="ImageCards(i).Bitmap = LoadBitmap(File.DirAssets, \"h\" & HeartCardValue(i) & \".bmp\")";
mostCurrent._imagecards[_i].setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"h"+BA.NumberToString(_heartcardvalue[_i])+".bmp").getObject()));
 //BA.debugLineNum = 266;BA.debugLine="ImageCards(i + 5).Bitmap = LoadBitmap(File.DirAssets, \"s\" & SpadeCardValue(i) & \".bmp\")";
mostCurrent._imagecards[(int)(_i+5)].setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"s"+BA.NumberToString(_spadecardvalue[_i])+".bmp").getObject()));
 //BA.debugLineNum = 267;BA.debugLine="LastBattle = LastBattle + 1 'Update the last battle flag.";
_lastbattle = (int)(_lastbattle+1);
 };
 }
};
 //BA.debugLineNum = 271;BA.debugLine="If SpadeCardsPlayed = 5 AND HeartCardsPlayed = 5 Then EndGame 'Check if all of the cards have been played.";
if (_spadecardsplayed==5 && _heartcardsplayed==5) { 
_endgame();};
 //BA.debugLineNum = 272;BA.debugLine="End Sub";
return "";
}
public static String  _buttonendturn_click() throws Exception{
int _i = 0;
 //BA.debugLineNum = 180;BA.debugLine="Sub ButtonEndTurn_Click";
 //BA.debugLineNum = 181;BA.debugLine="If GameOver = True Then";
if (_gameover==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 182;BA.debugLine="NewGame";
_newgame();
 //BA.debugLineNum = 183;BA.debugLine="Return";
if (true) return "";
 };
 //BA.debugLineNum = 186;BA.debugLine="If SoundEnabled Then 'Play the sound effect.";
if (_soundenabled) { 
 //BA.debugLineNum = 187;BA.debugLine="If HeartCardValue(5) = 0 OR SpadeCardValue(5) = 0 Then SP.Play(SoundEndTurn, 1, 1, 1, 0, 1)";
if (_heartcardvalue[(int)(5)]==0 || _spadecardvalue[(int)(5)]==0) { 
_sp.Play(_soundendturn,(float)(1),(float)(1),(int)(1),(int)(0),(float)(1));};
 };
 //BA.debugLineNum = 190;BA.debugLine="If CardsPlayedThisTurn = 0 Then 'Check if the player has played a card.";
if (_cardsplayedthisturn==0) { 
 //BA.debugLineNum = 191;BA.debugLine="If PlayerTurn = \"h\" Then";
if ((mostCurrent._playerturn).equals("h")) { 
 //BA.debugLineNum = 192;BA.debugLine="If HeartCardValue(5) < 1 Then";
if (_heartcardvalue[(int)(5)]<1) { 
 //BA.debugLineNum = 193;BA.debugLine="Msgbox(\"You must play a card.\", \"Error\")";
anywheresoftware.b4a.keywords.Common.Msgbox("You must play a card.","Error",mostCurrent.activityBA);
 //BA.debugLineNum = 194;BA.debugLine="Return";
if (true) return "";
 };
 }else {
 //BA.debugLineNum = 197;BA.debugLine="If PlayerTurn = \"s\" Then";
if ((mostCurrent._playerturn).equals("s")) { 
 //BA.debugLineNum = 198;BA.debugLine="If SpadeCardValue(5) < 1 Then";
if (_spadecardvalue[(int)(5)]<1) { 
 //BA.debugLineNum = 199;BA.debugLine="Msgbox(\"You must play a card.\", \"Error\")";
anywheresoftware.b4a.keywords.Common.Msgbox("You must play a card.","Error",mostCurrent.activityBA);
 //BA.debugLineNum = 200;BA.debugLine="Return";
if (true) return "";
 };
 };
 };
 };
 //BA.debugLineNum = 206;BA.debugLine="Select Case RuleSet 'Check which rules are being used.";
switch (_ruleset) {
case 1:
 break;
case 2:
 //BA.debugLineNum = 209;BA.debugLine="If PlayerTurn <> StartingTurn AND CardsPlayedThisTurn < CardsPlayedLastTurn Then";
if ((mostCurrent._playerturn).equals(mostCurrent._startingturn) == false && _cardsplayedthisturn<_cardsplayedlastturn) { 
 //BA.debugLineNum = 210;BA.debugLine="Msgbox(\"You must play the same number of cards as your opponent.\", \"Thai Rules\")";
anywheresoftware.b4a.keywords.Common.Msgbox("You must play the same number of cards as your opponent.","Thai Rules",mostCurrent.activityBA);
 //BA.debugLineNum = 211;BA.debugLine="Return";
if (true) return "";
 };
 break;
case 3:
 break;
case 4:
 //BA.debugLineNum = 215;BA.debugLine="If CardsPlayedThisTurn < 5 Then";
if (_cardsplayedthisturn<5) { 
 //BA.debugLineNum = 216;BA.debugLine="Msgbox(\"You must play all five cards.\", \"Casual Rules B\")";
anywheresoftware.b4a.keywords.Common.Msgbox("You must play all five cards.","Casual Rules B",mostCurrent.activityBA);
 //BA.debugLineNum = 217;BA.debugLine="Return";
if (true) return "";
 };
 break;
}
;
 //BA.debugLineNum = 221;BA.debugLine="Battle";
_battle();
 //BA.debugLineNum = 223;BA.debugLine="CardsPlayedLastTurn = CardsPlayedThisTurn";
_cardsplayedlastturn = _cardsplayedthisturn;
 //BA.debugLineNum = 224;BA.debugLine="CardsPlayedThisTurn = 0";
_cardsplayedthisturn = (int)(0);
 //BA.debugLineNum = 226;BA.debugLine="If PlayerTurn = \"h\" Then 'Change the player's turn.";
if ((mostCurrent._playerturn).equals("h")) { 
 //BA.debugLineNum = 227;BA.debugLine="PlayerTurn = \"s\"";
mostCurrent._playerturn = "s";
 }else {
 //BA.debugLineNum = 229;BA.debugLine="PlayerTurn = \"h\"";
mostCurrent._playerturn = "h";
 };
 //BA.debugLineNum = 232;BA.debugLine="For i = 1 To 5";
{
final double step166 = 1;
final double limit166 = (int)(5);
for (_i = (int)(1); (step166 > 0 && _i <= limit166) || (step166 < 0 && _i >= limit166); _i += step166) {
 //BA.debugLineNum = 233;BA.debugLine="ImageCards(i + 10).Bitmap = LoadBitmap(File.DirAssets, PlayerTurn & i & \".bmp\") 'Reset the player's hand.";
mostCurrent._imagecards[(int)(_i+10)].setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),mostCurrent._playerturn+BA.NumberToString(_i)+".bmp").getObject()));
 //BA.debugLineNum = 234;BA.debugLine="ImageCards(i + 10).Visible = True";
mostCurrent._imagecards[(int)(_i+10)].setVisible(anywheresoftware.b4a.keywords.Common.True);
 }
};
 //BA.debugLineNum = 237;BA.debugLine="For i = 1 To 5";
{
final double step170 = 1;
final double limit170 = (int)(5);
for (_i = (int)(1); (step170 > 0 && _i <= limit170) || (step170 < 0 && _i >= limit170); _i += step170) {
 //BA.debugLineNum = 238;BA.debugLine="If PlayerTurn = \"h\" Then";
if ((mostCurrent._playerturn).equals("h")) { 
 //BA.debugLineNum = 239;BA.debugLine="If HeartCardValue(i) > 0 Then ImageCards(HeartCardValue(i) + 10).Visible = False";
if (_heartcardvalue[_i]>0) { 
mostCurrent._imagecards[(int)(_heartcardvalue[_i]+10)].setVisible(anywheresoftware.b4a.keywords.Common.False);};
 }else {
 //BA.debugLineNum = 241;BA.debugLine="If SpadeCardValue(i) > 0 Then ImageCards(SpadeCardValue(i) + 10).Visible = False";
if (_spadecardvalue[_i]>0) { 
mostCurrent._imagecards[(int)(_spadecardvalue[_i]+10)].setVisible(anywheresoftware.b4a.keywords.Common.False);};
 };
 }
};
 //BA.debugLineNum = 245;BA.debugLine="End Sub";
return "";
}
public static String  _endgame() throws Exception{
 //BA.debugLineNum = 311;BA.debugLine="Sub EndGame";
 //BA.debugLineNum = 312;BA.debugLine="If SoundEnabled Then SP.Play(SoundEndGame, 1, 1, 1, 0, 1) 'Play the sound effect.";
if (_soundenabled) { 
_sp.Play(_soundendgame,(float)(1),(float)(1),(int)(1),(int)(0),(float)(1));};
 //BA.debugLineNum = 314;BA.debugLine="GameOver = True";
_gameover = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 315;BA.debugLine="ButtonEndTurn.Text = \"Play Again\" 'Change the text of the button.";
mostCurrent._buttonendturn.setText((Object)("Play Again"));
 //BA.debugLineNum = 317;BA.debugLine="If HeartBattleWins > SpadeBattleWins Then 'Check which player won the game.";
if (_heartbattlewins>_spadebattlewins) { 
 //BA.debugLineNum = 318;BA.debugLine="Msgbox(\"Hearts Win!\", \"Game Over\")";
anywheresoftware.b4a.keywords.Common.Msgbox("Hearts Win!","Game Over",mostCurrent.activityBA);
 //BA.debugLineNum = 319;BA.debugLine="HeartScore = HeartScore + 1";
_heartscore = (int)(_heartscore+1);
 };
 //BA.debugLineNum = 321;BA.debugLine="If HeartBattleWins < SpadeBattleWins Then";
if (_heartbattlewins<_spadebattlewins) { 
 //BA.debugLineNum = 322;BA.debugLine="Msgbox(\"Spades Win!\", \"Game Over\")";
anywheresoftware.b4a.keywords.Common.Msgbox("Spades Win!","Game Over",mostCurrent.activityBA);
 //BA.debugLineNum = 323;BA.debugLine="SpadeScore = SpadeScore + 1";
_spadescore = (int)(_spadescore+1);
 };
 //BA.debugLineNum = 325;BA.debugLine="If HeartBattleWins = SpadeBattleWins Then";
if (_heartbattlewins==_spadebattlewins) { 
 //BA.debugLineNum = 326;BA.debugLine="Msgbox(\"It's a tie.\", \"Game Over\")";
anywheresoftware.b4a.keywords.Common.Msgbox("It's a tie.","Game Over",mostCurrent.activityBA);
 };
 //BA.debugLineNum = 329;BA.debugLine="LabelHeartScore.Text = \"Player One: \" & HeartScore 'Update the scoreboard.";
mostCurrent._labelheartscore.setText((Object)("Player One: "+BA.NumberToString(_heartscore)));
 //BA.debugLineNum = 330;BA.debugLine="LabelSpadeScore.Text = \"Player Two: \" & SpadeScore";
mostCurrent._labelspadescore.setText((Object)("Player Two: "+BA.NumberToString(_spadescore)));
 //BA.debugLineNum = 332;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (processGlobalsRun == false) {
	    processGlobalsRun = true;
		try {
		        main._process_globals();
options._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
vis = vis | (options.mostCurrent != null);
return vis;}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 32;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 36;BA.debugLine="Dim PlayerTurn As String";
mostCurrent._playerturn = "";
 //BA.debugLineNum = 37;BA.debugLine="Dim StartingTurn As String";
mostCurrent._startingturn = "";
 //BA.debugLineNum = 39;BA.debugLine="Dim CardsPlayedLastTurn As Int";
_cardsplayedlastturn = 0;
 //BA.debugLineNum = 40;BA.debugLine="Dim CardsPlayedThisTurn As Int";
_cardsplayedthisturn = 0;
 //BA.debugLineNum = 42;BA.debugLine="Dim LastBattle As Int";
_lastbattle = 0;
 //BA.debugLineNum = 44;BA.debugLine="Dim HeartBattleWins As Int";
_heartbattlewins = 0;
 //BA.debugLineNum = 45;BA.debugLine="Dim SpadeBattleWins As Int";
_spadebattlewins = 0;
 //BA.debugLineNum = 47;BA.debugLine="Dim HeartScore As Int";
_heartscore = 0;
 //BA.debugLineNum = 48;BA.debugLine="Dim SpadeScore As Int";
_spadescore = 0;
 //BA.debugLineNum = 50;BA.debugLine="Dim HeartCardValue(6) As Int";
_heartcardvalue = new int[(int)(6)];
;
 //BA.debugLineNum = 51;BA.debugLine="Dim SpadeCardValue(6) As Int";
_spadecardvalue = new int[(int)(6)];
;
 //BA.debugLineNum = 53;BA.debugLine="Dim HeartCardsPlayed As Int";
_heartcardsplayed = 0;
 //BA.debugLineNum = 54;BA.debugLine="Dim SpadeCardsPlayed As Int";
_spadecardsplayed = 0;
 //BA.debugLineNum = 56;BA.debugLine="Dim C1, C2, C3, C4, C5, C6, C7, C8, C9, C10, C11, C12, C13, C14, C15, C16 As ImageView";
mostCurrent._c1 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c2 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c3 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c4 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c5 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c6 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c7 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c8 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c9 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c10 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c11 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c12 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c13 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c14 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c15 = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._c16 = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 57;BA.debugLine="Dim ImageCards() As ImageView";
mostCurrent._imagecards = new anywheresoftware.b4a.objects.ImageViewWrapper[(int)(0)];
{
int d0 = mostCurrent._imagecards.length;
for (int i0 = 0;i0 < d0;i0++) {
mostCurrent._imagecards[i0] = new anywheresoftware.b4a.objects.ImageViewWrapper();
}
}
;
 //BA.debugLineNum = 59;BA.debugLine="Dim B1, B2, B3, B4, B5, B6 As Label";
mostCurrent._b1 = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._b2 = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._b3 = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._b4 = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._b5 = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._b6 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 60;BA.debugLine="Dim LabelBattle() As Label";
mostCurrent._labelbattle = new anywheresoftware.b4a.objects.LabelWrapper[(int)(0)];
{
int d0 = mostCurrent._labelbattle.length;
for (int i0 = 0;i0 < d0;i0++) {
mostCurrent._labelbattle[i0] = new anywheresoftware.b4a.objects.LabelWrapper();
}
}
;
 //BA.debugLineNum = 62;BA.debugLine="Dim PanelScoreBoard As Panel";
mostCurrent._panelscoreboard = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 64;BA.debugLine="Dim LabelHeartScore As Label";
mostCurrent._labelheartscore = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 65;BA.debugLine="Dim LabelSpadeScore As Label";
mostCurrent._labelspadescore = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 67;BA.debugLine="Dim ButtonEndTurn As Button";
mostCurrent._buttonendturn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 70;BA.debugLine="Dim GameOver As Boolean";
_gameover = false;
 //BA.debugLineNum = 72;BA.debugLine="End Sub";
return "";
}
public static String  _imagecard_click() throws Exception{
anywheresoftware.b4a.objects.ConcreteViewWrapper _cardclicked = null;
 //BA.debugLineNum = 139;BA.debugLine="Sub ImageCard_Click";
 //BA.debugLineNum = 142;BA.debugLine="Dim CardClicked As View";
_cardclicked = new anywheresoftware.b4a.objects.ConcreteViewWrapper();
 //BA.debugLineNum = 143;BA.debugLine="CardClicked = Sender";
_cardclicked.setObject((android.view.View)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 145;BA.debugLine="If CardClicked.Tag < 9 Then Return 'Check if the player clicked a playable card.";
if ((double)(BA.ObjectToNumber(_cardclicked.getTag()))<9) { 
if (true) return "";};
 //BA.debugLineNum = 147;BA.debugLine="If SoundEnabled Then SP.Play(SoundCardClick, 1, 1, 1, 0, 1) 'Play the sound effect.";
if (_soundenabled) { 
_sp.Play(_soundcardclick,(float)(1),(float)(1),(int)(1),(int)(0),(float)(1));};
 //BA.debugLineNum = 149;BA.debugLine="Select Case RuleSet 'Check which rules are being used.";
switch (_ruleset) {
case 1:
 break;
case 2:
 //BA.debugLineNum = 152;BA.debugLine="If PlayerTurn <> StartingTurn AND CardsPlayedThisTurn = CardsPlayedLastTurn AND CardsPlayedThisTurn > 0 Then";
if ((mostCurrent._playerturn).equals(mostCurrent._startingturn) == false && _cardsplayedthisturn==_cardsplayedlastturn && _cardsplayedthisturn>0) { 
 //BA.debugLineNum = 153;BA.debugLine="Msgbox(\"You must play the same number of cards as your opponent.\", \"Thai Rules\")";
anywheresoftware.b4a.keywords.Common.Msgbox("You must play the same number of cards as your opponent.","Thai Rules",mostCurrent.activityBA);
 //BA.debugLineNum = 154;BA.debugLine="Return";
if (true) return "";
 };
 break;
case 3:
 //BA.debugLineNum = 157;BA.debugLine="If CardsPlayedThisTurn = 1 Then";
if (_cardsplayedthisturn==1) { 
 //BA.debugLineNum = 158;BA.debugLine="Msgbox(\"You may only play one card per turn\", \"Casual Rules A\")";
anywheresoftware.b4a.keywords.Common.Msgbox("You may only play one card per turn","Casual Rules A",mostCurrent.activityBA);
 //BA.debugLineNum = 159;BA.debugLine="Return";
if (true) return "";
 };
 break;
case 4:
 break;
}
;
 //BA.debugLineNum = 164;BA.debugLine="ImageCards(CardClicked.Tag).Visible = False 'Play the clicked card and remove it from the player's hand.";
mostCurrent._imagecards[(int)(BA.ObjectToNumber(_cardclicked.getTag()))].setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 165;BA.debugLine="CardsPlayedThisTurn = CardsPlayedThisTurn + 1";
_cardsplayedthisturn = (int)(_cardsplayedthisturn+1);
 //BA.debugLineNum = 166;BA.debugLine="If PlayerTurn = \"h\" Then";
if ((mostCurrent._playerturn).equals("h")) { 
 //BA.debugLineNum = 167;BA.debugLine="HeartCardsPlayed = HeartCardsPlayed + 1";
_heartcardsplayed = (int)(_heartcardsplayed+1);
 //BA.debugLineNum = 168;BA.debugLine="ImageCards(HeartCardsPlayed).Visible = True";
mostCurrent._imagecards[_heartcardsplayed].setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 169;BA.debugLine="ImageCards(HeartCardsPlayed).Bitmap = LoadBitmap(File.DirAssets, PlayerTurn & \"facedown.bmp\")";
mostCurrent._imagecards[_heartcardsplayed].setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),mostCurrent._playerturn+"facedown.bmp").getObject()));
 //BA.debugLineNum = 170;BA.debugLine="HeartCardValue(HeartCardsPlayed) = (CardClicked.Tag - 10)";
_heartcardvalue[_heartcardsplayed] = (int)(((double)(BA.ObjectToNumber(_cardclicked.getTag()))-10));
 }else {
 //BA.debugLineNum = 172;BA.debugLine="SpadeCardsPlayed = SpadeCardsPlayed + 1";
_spadecardsplayed = (int)(_spadecardsplayed+1);
 //BA.debugLineNum = 173;BA.debugLine="ImageCards(SpadeCardsPlayed + 5).Visible = True";
mostCurrent._imagecards[(int)(_spadecardsplayed+5)].setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 174;BA.debugLine="ImageCards(SpadeCardsPlayed + 5).Bitmap = LoadBitmap(File.DirAssets, PlayerTurn & \"facedown.bmp\")";
mostCurrent._imagecards[(int)(_spadecardsplayed+5)].setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),mostCurrent._playerturn+"facedown.bmp").getObject()));
 //BA.debugLineNum = 175;BA.debugLine="SpadeCardValue(SpadeCardsPlayed) = (CardClicked.Tag - 10)";
_spadecardvalue[_spadecardsplayed] = (int)(((double)(BA.ObjectToNumber(_cardclicked.getTag()))-10));
 };
 //BA.debugLineNum = 178;BA.debugLine="End Sub";
return "";
}
public static String  _menu_click() throws Exception{
 //BA.debugLineNum = 334;BA.debugLine="Sub Menu_Click";
 //BA.debugLineNum = 335;BA.debugLine="Select Sender";
switch (BA.switchObjectToInt(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA),(Object)("Options"),(Object)("Help"),(Object)("Exit"))) {
case 0:
 //BA.debugLineNum = 337;BA.debugLine="StartActivity(\"Options\")";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)("Options"));
 break;
case 1:
 //BA.debugLineNum = 339;BA.debugLine="Msgbox(\"Cogito is a simple game to learn. Just try clicking stuff until you get it.\", \"Help\")";
anywheresoftware.b4a.keywords.Common.Msgbox("Cogito is a simple game to learn. Just try clicking stuff until you get it.","Help",mostCurrent.activityBA);
 break;
case 2:
 //BA.debugLineNum = 341;BA.debugLine="Activity.Finish";
mostCurrent._activity.Finish();
 break;
}
;
 //BA.debugLineNum = 343;BA.debugLine="End Sub";
return "";
}
public static String  _newgame() throws Exception{
int _i = 0;
 //BA.debugLineNum = 274;BA.debugLine="Sub NewGame";
 //BA.debugLineNum = 275;BA.debugLine="If SoundEnabled Then SP.Play(SoundNewGame, 1, 1, 1, 0, 1) 'Play the sound effect.";
if (_soundenabled) { 
_sp.Play(_soundnewgame,(float)(1),(float)(1),(int)(1),(int)(0),(float)(1));};
 //BA.debugLineNum = 277;BA.debugLine="GameOver = False 'Set the current turn to correct player and reset turn data.";
_gameover = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 278;BA.debugLine="ButtonEndTurn.Text = \"End Turn\"";
mostCurrent._buttonendturn.setText((Object)("End Turn"));
 //BA.debugLineNum = 280;BA.debugLine="If AlternateControl Then 'If indicated, change the starting player's turn.";
if (_alternatecontrol) { 
 //BA.debugLineNum = 281;BA.debugLine="If StartingTurn = \"h\" Then";
if ((mostCurrent._startingturn).equals("h")) { 
 //BA.debugLineNum = 282;BA.debugLine="StartingTurn = \"s\"";
mostCurrent._startingturn = "s";
 }else {
 //BA.debugLineNum = 284;BA.debugLine="StartingTurn = \"h\"";
mostCurrent._startingturn = "h";
 };
 };
 //BA.debugLineNum = 288;BA.debugLine="PlayerTurn = StartingTurn";
mostCurrent._playerturn = mostCurrent._startingturn;
 //BA.debugLineNum = 289;BA.debugLine="CardsPlayedThisTurn = 0";
_cardsplayedthisturn = (int)(0);
 //BA.debugLineNum = 290;BA.debugLine="CardsPlayedLastTurn = 0";
_cardsplayedlastturn = (int)(0);
 //BA.debugLineNum = 291;BA.debugLine="HeartBattleWins = 0";
_heartbattlewins = (int)(0);
 //BA.debugLineNum = 292;BA.debugLine="SpadeBattleWins = 0";
_spadebattlewins = (int)(0);
 //BA.debugLineNum = 293;BA.debugLine="HeartCardsPlayed = 0";
_heartcardsplayed = (int)(0);
 //BA.debugLineNum = 294;BA.debugLine="SpadeCardsPlayed = 0";
_spadecardsplayed = (int)(0);
 //BA.debugLineNum = 295;BA.debugLine="LastBattle = 0";
_lastbattle = (int)(0);
 //BA.debugLineNum = 297;BA.debugLine="For i = 1 To 5 'Reset the cards.";
{
final double step222 = 1;
final double limit222 = (int)(5);
for (_i = (int)(1); (step222 > 0 && _i <= limit222) || (step222 < 0 && _i >= limit222); _i += step222) {
 //BA.debugLineNum = 298;BA.debugLine="ImageCards(i).Bitmap = LoadBitmap(File.DirAssets, \"hfacedown.bmp\")";
mostCurrent._imagecards[_i].setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"hfacedown.bmp").getObject()));
 //BA.debugLineNum = 299;BA.debugLine="ImageCards(i + 5).Bitmap = LoadBitmap(File.DirAssets, \"sfacedown.bmp\")";
mostCurrent._imagecards[(int)(_i+5)].setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"sfacedown.bmp").getObject()));
 //BA.debugLineNum = 300;BA.debugLine="ImageCards(i + 10).Bitmap = LoadBitmap(File.DirAssets, PlayerTurn & i & \".bmp\")";
mostCurrent._imagecards[(int)(_i+10)].setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),mostCurrent._playerturn+BA.NumberToString(_i)+".bmp").getObject()));
 //BA.debugLineNum = 301;BA.debugLine="ImageCards(i).Visible = False";
mostCurrent._imagecards[_i].setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 302;BA.debugLine="ImageCards(i + 5).Visible = False";
mostCurrent._imagecards[(int)(_i+5)].setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 303;BA.debugLine="ImageCards(i + 10).Visible = True";
mostCurrent._imagecards[(int)(_i+10)].setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 304;BA.debugLine="LabelBattle(i).Text = \"\"";
mostCurrent._labelbattle[_i].setText((Object)(""));
 //BA.debugLineNum = 305;BA.debugLine="HeartCardValue(i) = 0";
_heartcardvalue[_i] = (int)(0);
 //BA.debugLineNum = 306;BA.debugLine="SpadeCardValue(i) = 0";
_spadecardvalue[_i] = (int)(0);
 }
};
 //BA.debugLineNum = 309;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 19;BA.debugLine="Dim ResetFlag As Boolean";
_resetflag = false;
 //BA.debugLineNum = 20;BA.debugLine="Dim AlternateControl As Boolean";
_alternatecontrol = false;
 //BA.debugLineNum = 21;BA.debugLine="Dim RuleSet As Int";
_ruleset = 0;
 //BA.debugLineNum = 23;BA.debugLine="Dim SoundEnabled As Boolean";
_soundenabled = false;
 //BA.debugLineNum = 24;BA.debugLine="Dim SP As SoundPool";
_sp = new anywheresoftware.b4a.audio.SoundPoolWrapper();
 //BA.debugLineNum = 25;BA.debugLine="Dim SoundCardClick As Int";
_soundcardclick = 0;
 //BA.debugLineNum = 26;BA.debugLine="Dim SoundEndTurn As Int";
_soundendturn = 0;
 //BA.debugLineNum = 27;BA.debugLine="Dim SoundEndGame As Int";
_soundendgame = 0;
 //BA.debugLineNum = 28;BA.debugLine="Dim SoundNewGame As Int";
_soundnewgame = 0;
 //BA.debugLineNum = 30;BA.debugLine="End Sub";
return "";
}
}
