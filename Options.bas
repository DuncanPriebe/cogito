Type=Activity
Version=2.52
@EndOfDesignText@
#Region  Activity Attributes 
	#FullScreen: True
	#IncludeTitle: False
#End Region
'Activity module
Sub Process_Globals
    'These global variables will be declared once when the application starts.
    'These variables can be accessed from all modules.

End Sub

Sub Globals
    'These global variables will be redeclared each time the activity is created.
    'These variables can only be accessed from this module.
Dim PreviousRuleSet As Int

Dim R1, R2, R3, R4, R5 As RadioButton
Dim RadioRuleOption() As RadioButton
RadioRuleOption = Array As RadioButton(R1, R2, R3, R4, R5)

Dim RT1, RT2, RT3, RT4, RT5 As String
Dim RadioRuleOptionText() As String
RadioRuleOptionText = Array As String(RT1, RT2, RT3, RT4, RT5)

Dim CheckBoxAlternateControl As CheckBox
Dim CheckBoxSoundEnabled As CheckBox

Dim ButtonExitOptions As Button

End Sub

Sub Activity_Create(FirstTime As Boolean)

For i = 1 To 4
    RadioRuleOption(i).Initialize("RuleOption")
Next

RadioRuleOptionText(1) = "Business rules"
RadioRuleOptionText(2) = "Thai rules"
RadioRuleOptionText(3) = "Casual rules A"
RadioRuleOptionText(4) = "Casual rules B"

Activity.LoadLayout("Cogito_Options") 'Load the options layout.

For i = 1 To 4
    RadioRuleOption(i).Text = RadioRuleOptionText(i)
    Activity.AddView(RadioRuleOption(i), 10%x, (i - 1) * 15%y, 40%x, 10%y)
Next

CheckBoxAlternateControl.Initialize("")
CheckBoxAlternateControl.Checked = Main.AlternateControl
CheckBoxAlternateControl.Text = "Alternate suits"
Activity.AddView(CheckBoxAlternateControl, 50%x, 0, 50%x, 10%y)

CheckBoxSoundEnabled.Initialize("")
CheckBoxSoundEnabled.Checked = Main.SoundEnabled
CheckBoxSoundEnabled.Text = "Enable sound"
Activity.AddView(CheckBoxSoundEnabled, 50%x, 15%y, 50%x, 10%y)

ButtonExitOptions.Initialize("ButtonExitOptions")
Activity.AddView(ButtonExitOptions, 40%x, 82.5%y, 20%x, 15%y)
ButtonExitOptions.Text = "Exit Options"

PreviousRuleSet = Main.RuleSet 'Remember what the rule set was before it was changed.

RadioRuleOption(Main.RuleSet).Checked = True 'Set the correct option to be selected by default.

End Sub
Sub ButtonExitOptions_Click
For i = 1 To 4
    If RadioRuleOption(i).Checked = True Then Main.RuleSet = i 'Change rule set based on which button is selected.
Next

If Main.RuleSet <> PreviousRuleSet Then
    Msgbox("Changing the rules will reset the game.", "Warning!") 'Notify the player that the rules have changed and reset the game.
    PreviousRuleSet = Main.RuleSet
    Main.ResetFlag = True 'Signal the main module that the game should be reset.
End If

Main.AlternateControl = CheckBoxAlternateControl.Checked
Main.SoundEnabled = CheckBoxSoundEnabled.Checked

Activity.Finish 'Close the options activity.

End Sub
Sub Activity_Resume

End Sub

Sub Activity_Pause (UserClosed As Boolean)

End Sub

