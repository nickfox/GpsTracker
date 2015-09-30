Just a note about the 2 android clients (android and android-wp). The android-wp client is for the wordpress plugin and the android client is for the php website. They will not work if they are swapped. I may decide to create one client for both later but don't feel like doing it right now.

The difference between the two is that the wordpress client gets a "nonce" from the wordpress plugin:

https://codex.wordpress.org/WordPress_Nonces

This is used for added security so that apps that have not been properly verified will not be able to send updates to the plugin. A nice safety feature.