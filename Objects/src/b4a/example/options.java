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

public class options extends Activity implements B4AActivity{
	public static options mostCurrent;
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
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.options");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                anywheresoftware.b4a.keywords.Common.Log("Killing previous instance (options).");
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
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.options");
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        initializeProcessGlobals();		
        initializeGlobals();
        
        anywheresoftware.b4a.keywords.Common.Log("** Activity (options) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        anywheresoftware.b4a.keywords.Common.Log("** Activity (options) Resume **");
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
		return options.class;
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
        anywheresoftware.b4a.keywords.Common.Log("** Activity (options) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
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
            anywheresoftware.b4a.keywords.Common.Log("** Activity (options) Resume **");
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
public static int _previousruleset = 0;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _r1 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _r2 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _r3 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _r4 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _r5 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper[] _radioruleoption = null;
public static String _rt1 = "";
public static String _rt2 = "";
public static String _rt3 = "";
public static String _rt4 = "";
public static String _rt5 = "";
public static String[] _radioruleoptiontext = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _checkboxalternatecontrol = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _checkboxsoundenabled = null;
public anywheresoftware.b4a.objects.ButtonWrapper _buttonexitoptions = null;
public b4a.example.main _main = null;
public static String  _activity_create(boolean _firsttime) throws Exception{
int _i = 0;
 //BA.debugLineNum = 32;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 34;BA.debugLine="For i = 1 To 4";
{
final double step15 = 1;
final double limit15 = (int)(4);
for (_i = (int)(1); (step15 > 0 && _i <= limit15) || (step15 < 0 && _i >= limit15); _i += step15) {
 //BA.debugLineNum = 35;BA.debugLine="RadioRuleOption(i).Initialize(\"RuleOption\")";
mostCurrent._radioruleoption[_i].Initialize(mostCurrent.activityBA,"RuleOption");
 }
};
 //BA.debugLineNum = 38;BA.debugLine="RadioRuleOptionText(1) = \"Business rules\"";
mostCurrent._radioruleoptiontext[(int)(1)] = "Business rules";
 //BA.debugLineNum = 39;BA.debugLine="RadioRuleOptionText(2) = \"Thai rules\"";
mostCurrent._radioruleoptiontext[(int)(2)] = "Thai rules";
 //BA.debugLineNum = 40;BA.debugLine="RadioRuleOptionText(3) = \"Casual rules A\"";
mostCurrent._radioruleoptiontext[(int)(3)] = "Casual rules A";
 //BA.debugLineNum = 41;BA.debugLine="RadioRuleOptionText(4) = \"Casual rules B\"";
mostCurrent._radioruleoptiontext[(int)(4)] = "Casual rules B";
 //BA.debugLineNum = 43;BA.debugLine="Activity.LoadLayout(\"Cogito_Options\") 'Load the options layout.";
mostCurrent._activity.LoadLayout("Cogito_Options",mostCurrent.activityBA);
 //BA.debugLineNum = 45;BA.debugLine="For i = 1 To 4";
{
final double step23 = 1;
final double limit23 = (int)(4);
for (_i = (int)(1); (step23 > 0 && _i <= limit23) || (step23 < 0 && _i >= limit23); _i += step23) {
 //BA.debugLineNum = 46;BA.debugLine="RadioRuleOption(i).Text = RadioRuleOptionText(i)";
mostCurrent._radioruleoption[_i].setText((Object)(mostCurrent._radioruleoptiontext[_i]));
 //BA.debugLineNum = 47;BA.debugLine="Activity.AddView(RadioRuleOption(i), 10%x, (i - 1) * 15%y, 40%x, 10%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._radioruleoption[_i].getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(10),mostCurrent.activityBA),(int)((_i-1)*anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(15),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(10),mostCurrent.activityBA));
 }
};
 //BA.debugLineNum = 50;BA.debugLine="CheckBoxAlternateControl.Initialize(\"\")";
mostCurrent._checkboxalternatecontrol.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 51;BA.debugLine="CheckBoxAlternateControl.Checked = Main.AlternateControl";
mostCurrent._checkboxalternatecontrol.setChecked(mostCurrent._main._alternatecontrol);
 //BA.debugLineNum = 52;BA.debugLine="CheckBoxAlternateControl.Text = \"Alternate suits\"";
mostCurrent._checkboxalternatecontrol.setText((Object)("Alternate suits"));
 //BA.debugLineNum = 53;BA.debugLine="Activity.AddView(CheckBoxAlternateControl, 50%x, 0, 50%x, 10%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._checkboxalternatecontrol.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(50),mostCurrent.activityBA),(int)(0),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(10),mostCurrent.activityBA));
 //BA.debugLineNum = 55;BA.debugLine="CheckBoxSoundEnabled.Initialize(\"\")";
mostCurrent._checkboxsoundenabled.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 56;BA.debugLine="CheckBoxSoundEnabled.Checked = Main.SoundEnabled";
mostCurrent._checkboxsoundenabled.setChecked(mostCurrent._main._soundenabled);
 //BA.debugLineNum = 57;BA.debugLine="CheckBoxSoundEnabled.Text = \"Enable sound\"";
mostCurrent._checkboxsoundenabled.setText((Object)("Enable sound"));
 //BA.debugLineNum = 58;BA.debugLine="Activity.AddView(CheckBoxSoundEnabled, 50%x, 15%y, 50%x, 10%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._checkboxsoundenabled.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(10),mostCurrent.activityBA));
 //BA.debugLineNum = 60;BA.debugLine="ButtonExitOptions.Initialize(\"ButtonExitOptions\")";
mostCurrent._buttonexitoptions.Initialize(mostCurrent.activityBA,"ButtonExitOptions");
 //BA.debugLineNum = 61;BA.debugLine="Activity.AddView(ButtonExitOptions, 40%x, 82.5%y, 20%x, 15%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._buttonexitoptions.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(82.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float)(20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float)(15),mostCurrent.activityBA));
 //BA.debugLineNum = 62;BA.debugLine="ButtonExitOptions.Text = \"Exit Options\"";
mostCurrent._buttonexitoptions.setText((Object)("Exit Options"));
 //BA.debugLineNum = 64;BA.debugLine="PreviousRuleSet = Main.RuleSet 'Remember what the rule set was before it was changed.";
_previousruleset = mostCurrent._main._ruleset;
 //BA.debugLineNum = 66;BA.debugLine="RadioRuleOption(Main.RuleSet).Checked = True 'Set the correct option to be selected by default.";
mostCurrent._radioruleoption[mostCurrent._main._ruleset].setChecked(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 68;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 90;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 92;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 86;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 88;BA.debugLine="End Sub";
return "";
}
public static String  _buttonexitoptions_click() throws Exception{
int _i = 0;
 //BA.debugLineNum = 69;BA.debugLine="Sub ButtonExitOptions_Click";
 //BA.debugLineNum = 70;BA.debugLine="For i = 1 To 4";
{
final double step42 = 1;
final double limit42 = (int)(4);
for (_i = (int)(1); (step42 > 0 && _i <= limit42) || (step42 < 0 && _i >= limit42); _i += step42) {
 //BA.debugLineNum = 71;BA.debugLine="If RadioRuleOption(i).Checked = True Then Main.RuleSet = i 'Change rule set based on which button is selected.";
if (mostCurrent._radioruleoption[_i].getChecked()==anywheresoftware.b4a.keywords.Common.True) { 
mostCurrent._main._ruleset = _i;};
 }
};
 //BA.debugLineNum = 74;BA.debugLine="If Main.RuleSet <> PreviousRuleSet Then";
if (mostCurrent._main._ruleset!=_previousruleset) { 
 //BA.debugLineNum = 75;BA.debugLine="Msgbox(\"Changing the rules will reset the game.\", \"Warning!\") 'Notify the player that the rules have changed and reset the game.";
anywheresoftware.b4a.keywords.Common.Msgbox("Changing the rules will reset the game.","Warning!",mostCurrent.activityBA);
 //BA.debugLineNum = 76;BA.debugLine="PreviousRuleSet = Main.RuleSet";
_previousruleset = mostCurrent._main._ruleset;
 //BA.debugLineNum = 77;BA.debugLine="Main.ResetFlag = True 'Signal the main module that the game should be reset.";
mostCurrent._main._resetflag = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 80;BA.debugLine="Main.AlternateControl = CheckBoxAlternateControl.Checked";
mostCurrent._main._alternatecontrol = mostCurrent._checkboxalternatecontrol.getChecked();
 //BA.debugLineNum = 81;BA.debugLine="Main.SoundEnabled = CheckBoxSoundEnabled.Checked";
mostCurrent._main._soundenabled = mostCurrent._checkboxsoundenabled.getChecked();
 //BA.debugLineNum = 83;BA.debugLine="Activity.Finish 'Close the options activity.";
mostCurrent._activity.Finish();
 //BA.debugLineNum = 85;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
             try {
                Class.forName(BA.applicationContext.getPackageName() + ".main").getMethod("initializeProcessGlobals").invoke(null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 12;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 15;BA.debugLine="Dim PreviousRuleSet As Int";
_previousruleset = 0;
 //BA.debugLineNum = 17;BA.debugLine="Dim R1, R2, R3, R4, R5 As RadioButton";
mostCurrent._r1 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
mostCurrent._r2 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
mostCurrent._r3 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
mostCurrent._r4 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
mostCurrent._r5 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 18;BA.debugLine="Dim RadioRuleOption() As RadioButton";
mostCurrent._radioruleoption = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper[(int)(0)];
{
int d0 = mostCurrent._radioruleoption.length;
for (int i0 = 0;i0 < d0;i0++) {
mostCurrent._radioruleoption[i0] = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
}
}
;
 //BA.debugLineNum = 19;BA.debugLine="RadioRuleOption = Array As RadioButton(R1, R2, R3, R4, R5)";
mostCurrent._radioruleoption = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper[]{mostCurrent._r1,mostCurrent._r2,mostCurrent._r3,mostCurrent._r4,mostCurrent._r5};
 //BA.debugLineNum = 21;BA.debugLine="Dim RT1, RT2, RT3, RT4, RT5 As String";
mostCurrent._rt1 = "";
mostCurrent._rt2 = "";
mostCurrent._rt3 = "";
mostCurrent._rt4 = "";
mostCurrent._rt5 = "";
 //BA.debugLineNum = 22;BA.debugLine="Dim RadioRuleOptionText() As String";
mostCurrent._radioruleoptiontext = new String[(int)(0)];
java.util.Arrays.fill(mostCurrent._radioruleoptiontext,"");
 //BA.debugLineNum = 23;BA.debugLine="RadioRuleOptionText = Array As String(RT1, RT2, RT3, RT4, RT5)";
mostCurrent._radioruleoptiontext = new String[]{mostCurrent._rt1,mostCurrent._rt2,mostCurrent._rt3,mostCurrent._rt4,mostCurrent._rt5};
 //BA.debugLineNum = 25;BA.debugLine="Dim CheckBoxAlternateControl As CheckBox";
mostCurrent._checkboxalternatecontrol = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Dim CheckBoxSoundEnabled As CheckBox";
mostCurrent._checkboxsoundenabled = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Dim ButtonExitOptions As Button";
mostCurrent._buttonexitoptions = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 30;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 6;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 10;BA.debugLine="End Sub";
return "";
}
}
