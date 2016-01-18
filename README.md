# Simple Data Viewer

This is a small data viewer that you can use to visualize data in a SQLIte database.

## Build and install the application

To build the application, you should install the Java 8 SDK.
Then clone this repository and launch the maven wrapper.

    git clone https://github.com/jxerome/simple-data-viewer.git
    cd simple-data-viewer
    ./mvnw
    
Then you can install the application.
 
    SRC=$(pwd)
    cd ~/tmp
    tar xzvf $SRC/target/data-viewer-1.0.tar.gz


## Run the appplication

To run the application execute the `dw` script in `bin` dir.
Provides it with a list of SQLite files.

    ./dw my-data1.db my-data2.db
    
`dw` will launch a web server on port 8080 and open your default browser on the page.
 
 You can change the default port with option `-p`.
