# dailyLog

dailyLog enables you to rapid log with customizable shortcuts. Tired of trying to log workouts on your phone? Want to build a custom habit tracker? Looking for the fastest way to get notes into your Obsidian vault? dailyLog can help.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.app.dailylog/)

Or download the latest APK from the [Releases Section](https://github.com/madCode/dailylog/releases/latest).

Initially designed as a rapid logging system for journaling, dailyLog allows you to quickly add info to a text file with customizable button shortcuts. Here's how it works:

## Step 1: Select Your File
Opening the app for the first time will allow you to either create a file or select an existing one:

<img alt="screen that says 'Welcome' and two buttons: Create File and Select File" src="/README_screenshots/welcome_screen.png" width="40%"/>

If you select "Create a File", the app will navigate you to your Documents folder and autofill your file name as "journal.md". You can change this to whatever you would like, then hit save:

<img alt="Android's default file creation screen" src="/README_screenshots/create_file.png" width="40%"/>

Alternatively, you can click on "Select a file" if you have a file in mind. 

### If Android 11 or below
When the file selector window opens, navigate to "Internal storage" and _then_ navigate to your file. In order to get write access to the file, the app needs to know the full path of the file. Due to Android shenanigans, in some versions, the only way to get this is when you navigate to the exact folder the file is in, rather than selecting it from the "Recent" shortcuts menu. Here's what I mean:

<img alt="The Android file selection menu. The navigation menu is open and the 'SD Card' option is selected, not the 'Recent' option at the top." src="/README_screenshots/open_from_internal_storage.png" width="40%"/>

Note that in the screenshot it says "SD Card", but on most phones, it should say "Internal Storage" instead.

<img alt="The Documents folder inside of 'SD Card' has been selected. There are files here." src="/README_screenshots/open_from_internal_storage_2.png" width="40%"/>

# Step 2: Start Typing
Once you've selected or created the file, the app will take you to the log screen, which is optimized for distraction-free typing. There are no menus, and only two buttons in the corner: settings and save:

<img alt="Blank typing area as described above." src="/README_screenshots/blank_log_screen.png" width="40%"/>

Type something and hit the save button. You should see a Toast message saying "Saved file." If you get the "Saved file." message but the changes did _not_ persist (aka the file went back to how it was when you opened it), then you likely did not open the file from the Internal Storage section as outlined above. Check out [I can't edit the file I selected! When I edit and hit save, all my edits disappear!
](#i-cant-edit-the-file-i-selected-when-i-edit-and-hit-save-all-my-edits-disappear) or [Step 1: Select Your File](#step-1-select-your-file) for more.

# Step 3: Create your first shortcut
If you're using dailyLog, it's likely because you have a format you like to journal in and you need keyboard shortcuts to keep it fast and easy on your phone. Hit the gear icon on the log screen to navigate to the settings page:

<img alt="Screen as described below" src="/README_screenshots/blank_settings_screen.png" width="40%"/>

You'll see at the top the option to change the file you're working in, and under that a section labeled "Shortcuts" with a three dot menu next to it. At the bottom of the screen you'll see a big blue circle with a plus in it. Click the plus button.

<img alt="Shorcut creation dialog. Description below." src="/README_screenshots/empty_shortcut_create.png" width="40%"/>

You'll see a few items on the screen:
1. label: this is the "name" of the shortcut. I recommend something short, or even an emoji. Let's say you want a shortcut for "dinner", you might choose "d" or even 🥣 .
2. text: this is the "value" of the shortcut, the longform of your label. In the dinner example, you might choose "- dinner: " to create a bullet point ready to capture what you had for dinner.
3. cursor index: ignore this, it will be covered in [Cursor positioning](#cursor-positioning).
4. "Contains datetime format" switch: ignore this as well, it'll be covered in [Shortcuts with timestamps](#shortcuts-with-timestamps)

Your screen now looks like this:

<img alt="Shortcut creation dialog. All fields are filled out as described above. Anything not described above has been left at its default value." src="/README_screenshots/create_dinner.png" width="40%"/>

Hit the save button. We'll cover the other parts of this screen in the "Shortcuts with timestamps" section and the "Cursor positioning" sections.

You'll now see your shortcut displayed in the shortcuts list:

<img alt="The settings screen from before but now under the 'Shortcuts' heading there is your dinner shortcut. The label and text are there, with a | at the end to denote that we left the cursor index as the end." src="/README_screenshots/shortcut_list.png" width="40%"/>

Clicking on it will allow you to edit it. You'll also notice a gray x on the side to delete the shortcut, and the six gray dots on the left-hand side of the shortcut card. The dots denote that when there are multiple shortcuts, you can drag to reorder them. More in "Drag to reorder shortcuts".

## Step 4. Insert your first shortcut
Hit the back button to go back to the log screen. It should look like this:

<img alt="The blank log screen from before, but now there's a tray of buttons at the bottom containing our dinner shortcut button" src="/README_screenshots/shortcut_tray.png" width="40%"/>

Hit the 🥣 button, you'll see the text you chose inserted into the log.

<img alt="The log screen now contains '- dinner: '" src="/README_screenshots/paste_dinner.png" width="40%"/>

Tada! You've now successfully used dailyLog. To get a rundown of the other features, please continue reading:
# Other features
## Autosave
While the save button is available, the app also saves to your file every time you navigate away from the log screen. Going to the settings page or minimizing the app will both trigger file saves. While this is not the exact "Autosave" mechanism other apps may provide, in my experience using the app it's just as effective.
## Cursor positioning
1. In the log itself, the app remembers where your cursor was when you closed the app and brings you exactly back there. No matter if you journal in chronological or reverse chronological order, the app has your back.
2. Sometimes you have a complicated shortcut template and you want your cursor to be placed in a specific spot when you're done. Say, for example, we want to change our dinner shortcut to something like this: "- dinner: food{ }, location{ }, people{ }". We want space to track the food we ate, where we had dinner, and who we had dinner with. And after the shortcut gets pasted in, we want the cursor to be inside the food block so we're ready to fill it in. dailyLog supports this. Here's how.

Navigate back to the shortcut page and click on the dinner shortcut to edit it. 

<img alt="A screen filled out with all the info for dinner" src="/README_screenshots/edit_dinner.png" width="40%"/>

See the pink slider labelled "cursor index"? Notice that it's slider marker is all the way at the end. Now start modifying the body of the shortcut to say "- dinner: food{ }, location{ }, people{ }". You'll already see that the cursor index slider is no longer at the end. If you look underneath the "Edit shortcut" title, you'll see the body of your text, with a pink line denoting where the cursor is.

<img alt="Screen as described above" src="/README_screenshots/edit_dinner_2.png" width="40%"/>

Let's move the cursor slider until the pink line is inside the food's curly braces:

<img alt="Screen as described above" src="/README_screenshots/edit_dinner_3.png" width="40%"/>

Now, when you use the shortcut, your cursor will automatically be exactly where you want it to be!
## Shortcuts with timestamps
It wouldn't be a journaling app without timestamps. dailyLog supports adding timestamp regexes in shortcuts. Let's say you want a shortcut that adds today's date as an H1 title in your markdown file. For example: "# 03 March 1970".

Start by opening the "Create new shortcut" dialog. Let's make the label the calendar emoji: 📅, and then we need to fill in the text. dailyLog's format is: `{DATETIME: <whatever format you want>}`. So in our case that would look like "# {DATETIME: DD MMMM yyyy}". And make sure to toggle the "Contains datetime format" switch at the bottom like so:

<img alt="Screen as described above" src="/README_screenshots/datetime_example.png" width="40%"/>

Now if you hit the calendar shortcut, you get the date!
## Drag to reorder shortcuts
When you have a list of shortcuts in the settings page, you can drag them around to reorder them. This will also change the order by which they appear in the shortcuts tray.
## Export shortcuts
You can export your shortcuts to a csv file by hitting the three-dot menu next to "Shortcuts" in the settings page.

<img alt="An options menu has popped up with three options: Bulk add shortcuts, Export shortcuts, and Import Shortcuts from CSV" src="/README_screenshots/three_dot_menu.png" width="40%"/>

It will prompt you to select a place to create the csv file.
## Import shortcuts
This allows you to import shortcuts via a csv file. The csv file should not have a header and should have quotes around all values. The order of the row should be: label, text, cursorIndex, shortcutType. Valid types are either "TEXT" or "DATETIME". If you want to make a csv file from scratch, I recommend first making a few shortcuts in the app, exporting that file, and then looking at the file as you design your own.
## Bulk add shortcuts
The other option is to use the bulk add feature. This shows you a text box and allows you to type in values. You don't need to wrap everything in quotes, but you do still need to follow the label, text, cursorIndex, shortcutType order.

# Known Issues
## I can't edit the file I selected! When I edit and hit save, all my edits disappear!
You probably selected the file from the "Recents" menu or some other shortcut folder conveniently provided to you by Android's file picker. Android has some weird bug where in order for the app to get the full path to the file (and therefore write to the file), you _must_ select it via the Internal Storage directory. You must navigate to the file's exact location. See [Step 1: Select Your File](#step-1-select-your-file) for more details.
## I just selected a file and I'm seeing "File read permissions not granted.". But I definitely did grant permissions!
If you _just_ opened the app for the first time and _just_ granted permissions, you may still see a Toast notification saying "File read permissions not granted". Give the app about 30 seconds. The app is trying to load the file right after permissions have been granted and it may not work right away. After about 10-30 seconds you should see a "Saved file" message and all should be right with the world. If the Toast message has gone away, try hitting the save button and see if it works. If it does, you're all right.

If it's still not working, double-check that you _did_, in fact, give permissions to the app.
