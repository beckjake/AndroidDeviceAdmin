
# WARNING!
This is not a 'safe' application! It asks for permission to wipe your device because it can and will do so!
It is possible there are bugs and that they will cause your device to wipe itself. Obviously I've done my best to avoid that since I'm using this myself.

Also note that in the normal course of operation you should EXPECT THIS TO WIPE YOUR PHONE at ANY TIME too many wrong passwords are entered, including:
* The nefarious bad guy who wants to get into your email accounts and steal your credentials and identity, guesses wrong too many times, and wipes it.
* "The Man", who's after you, the nefarious bad guy, guesses wrong too many times, and wipes it.
* Your kid is playing with your phone, presses ok too many times, and wipes it.
* Your grandma trying to use your phone unlock pad to call someone, does so too many times, and wipes it.
* A well intentioned stranger who finds it and tries to "guess" your password so they can get your info and return it, does so too many times, and wipes it.
* You are drunk or otherwise making poor choices, and mistype your password too many times in a row, and wipe it.
* A jerk finds your phone and intentionally wipes it because they are a jerk.

That means being prepared: Keep backups, or be prepared to lose all yoru data, or don't use this feature.

I am not liable for this wiping your device and you losing your data/loved ones/livelihood, whether it be through bugs, mistakes on the user end, malice, etc.

Ok, on to the fun stuff.

# AndroidDeviceAdmin
An application to expose Android device administrative features that are otherwise hidden.

I wrote this because I want too many failed login attempts to wipe my phone, and that menu is apparently gone in Marshmallow.
Fortunately there's a nice [API](https://developer.android.com/guide/topics/admin/device-admin.html), so I can fix that.

There are also some nice little goodies (password policies, etc) that I found buried in here that I'd like to add.

## Privileges
It shouldn't be surprising to learn that this needs some scary privileges.
This app uses the Device Administrator permission.
It needs to be able to "erase all data" because the whole point is to erase all data on too many failed logins.
It needs to be able to "monitor screen-unlock attempts" because we need to keep track of successful and failed logins because, again, that's the whole point.

## Behavior notes
At least some settings reset when/if you lose device admin status. That's not the app, it's Android. The UI tries to reflect this behavior.

The UI for enabling device admin has a long, tedious warning to meet some requirements for the App store. Sorry it sucks. If you have suggestions for fixing it while remaining in compliance, patches are always welcome.

## Implemented so far
* Enabling/disabling admin privileges through the app itself
* Wipe after X failed attempts

## TODO
* Minimum number for failed attempts, below which you get confirmation pop-ups (3? 5? it's kind of scary now.)
* Find out if there's some way to unit/integration test the UI.
* Encryption requirements
* Password strength requirements
* Everything else useful/neat exposed via this API
* Create some neat client/server type thing with a C&C system. Android has their device finder thing but it doesn't set all these policies.


## License
This is MIT licensed. See LICENSE.md.
