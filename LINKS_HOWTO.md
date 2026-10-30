# Links howto

The “*links*” feature in **PdDroidParty** allows users to share online Pd patches, recursively.

The patches must be available for download in the form of *pdz* files, which are zip files renamed with the extension `.pdz`. The compressed directory must contain an entry Pd patch called `droidparty_main.pd`.


## Structure of the "links" file

Any file named `links.txt` located in the PdDroidParty external storage directory (normally `/sdcard/Android/data/cx.mccormick.pddroidparty/files`), or in its subdirectories, is analyzed this way:

- if a line starts with "`# LINKS`", only the following lines are considered
- if a line starts with "`# ENDLINKS`", only the preceding are considered  
 (note if no line starts with "`# LINKS`" or "`# ENDLINKS`", the whole file is taken into account)
- each remaining line of the file is checked to see if it is a valid link. 

A link line must contain at least 3 words (no spaces allowed...): **`TYPE`** **`NAME`** **`URL`**

**`NAME`** is the name of the subdirectory or item that will be added to the directory where the *links* file is located.  
**`URL`** is a web address.  
**`TYPE`** is used to determine how to use `URL`.

## Link types

The allowed types are:

### **`- "pdz"`**

**`URL`** points to a downloadable pdz file. **`NAME`** must match the name of the compressed directory in the pdz file.

#### Example:

A file named `links.txt` and located in the `user_patches/` directory in PdDroidParty external storage, contains the line:  
**`pdz piano https://janedoe.com/pddroidpatches/piano.pdz`**  

An entry named **`piano`** is added to the `user_patches/` directory in the PatchSelector.  
This entry is marked as *downloadable* and the patch can be installed on demand, after what it becomes *playable*.  
When played, Pd loads `/sdcard/Android/data/cx.mccormick.pddroidparty/files/user_patches/piano/droidparty_main.pd`.

### **`- "links"`**
`URL` points to a downloadable *links* file, which will be parsed the same way.

#### Example:

A file named `links.txt` and located in the `user_patches/` directory, contains the line:  
**`links janesynths https://janedoe.com/pddroidpatches/catalog.txt`**  

A subdirectory called **`janesynths`** is created in the `user_patches/` directory; when the user enters this subdirectory, the file `catalog.txt` is downloaded and analyzed as a *links* file and can, in turn, create new items or subdirectories in `user_patches/janesynths/`.

### **`- "gh"`**
`URL` points to a server that provides the GitHub API or a compatible API.

A **`gh`** link can point to different GitHub-API-like contents:

- the **directory** of a repository, viewed as a local storage directory. Ex:  
**`gh janedoe_synths https://api.github.com/repos/janedoe/PdDroidPatches/contents/synths`**  
points to the `synths` directory of the `janedoe/PdDroidPatches` Gihub repo.  
This directory can contain pdz files, a `links.txt` file, or subdirectories themselves containing pdz files, `links.txt` or subdirs, and so on.

- the **release** of a github repo, like:  
**`gh janedoe_synths https://api.github.com/repos/janedoe/PdDroidPatches/releases/tags/synthstag`**  
In this case, the **description text** of the release (the `body` field in the JSON) is treated as a *links* file, and its lines (only between "`# LINKS`" and "`# ENDLINKS`" lines if present) are parsed for links.  
Moreover, the **release assets** (the `assets` array field of the JSON) are taken into account too: if an asset is named `links.txt`, it is parsed for links; an asset named `*.pdz` is made available for installation.



