# jPlace
A Java-based recreation of reddit's /r/place.

## How to host:
* Server-side: compile src/place/server/PlaceServer.java
* Run: 
`$ java place.server.PlaceServer hostname dimensions port_number`

## How to play:
* Client-side GUI: compile place/client/gui/PlaceGUI
* Run:
`$ java place.client.gui.PlaceGUI --hostname="hostname" --port="port_number" --username="unique_identifier"`
* Client-side PTUI: compile and run place/client/ptui/PlacePTUI
* Run:
`$ java place.client.ptui.PlacePTUI hostname port username`

## Using bots:
A multitude of bots are included with the packages. To run them, compile and run the following commands for your bot:
* Single-tile bot:
`$ java place.bots.SingleTileBot hostname port_number username row col color time_for_each_input`
* Random bot:
`$ java place.bots.SingleTileBot hostname port_number username time_for_each_input`
* Row by Column bot:
`$ java place.bots.SingleTileBot hostname port_number username colour time_for_each_input`
* Protection bot:
`$ java place.bots.SingleTileBot hostname port_number username start_row start_col end_row end_col time_for_each_input`
