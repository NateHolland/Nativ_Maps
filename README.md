# Nativ_Maps
Android library for creating and editing gps routes

## Download
Add the Jitpack repository to your root build file:

Add it in your root settings.gradle at the end of repositories:

	dependencyResolutionManagement {
    		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Add the dependency

	dependencies {
	        implementation 'com.github.NateHolland:Nativ_Maps:Tag'
	}
	
## Dependencies

* [Android GPX Parser](https://github.com/ticofab/android-gpx-parser)

## Usage

	<nativ.tech.routes.RouteEditView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

	
Add view to your xml layouts

To save route use export() and write the output to a file

To open a route use Route.fromFile() to return a route object

To import a gpx route use GeoCalc.importGpx() to return a route object

See the example app for basic implementation.

## Contribute
Contributions are welcome! Please check the issues and open a pull request when done.

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
