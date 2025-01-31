## Environment setup

### DTU hosted development environment
If this session is hosted by Dynatrace University, any preparation work has been done for you.
What's left is to access your Dynatrace Environment and the DTU Dashboard.

* Click on `View Environment` to log into your Dynatrace Environment
* Clicking on `Open Terminal` will reveal the link to your DTU Dashboard
* Within the DTU Dashboard click on `Link` at the top of the screen
* Click on the link for `VSCode` to open up your development environment    
* Within the terminal at the bottom type in
```
docker compose up -d --build
```

You can reuse that terminal for the rest of the session.

<img src="../../../assets/images/01_setup_00_launch_ace_box.gif" alt="ACEBox" style="width:500px" />

### GitHub CodeSpaces
In case you are using GitHub CodeSpaces (outside of this DTU Event), follow the rest of the instructions in this section.

1. Create Dynatrace Access Token
2. Configure and Start your GitHub Code Space
3. Launch the Demo Application