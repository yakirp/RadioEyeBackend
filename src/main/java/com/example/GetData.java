package com.example;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.pubnub.api.PubnubSyncedObject;

//http://localhost:8080/RadioEyeApi/rest/get/table/users.yakir
//Sets the path to base URL + /get
@Path("/get")
public class GetData {

	@GET
	@Path("/{objectId}/{path}")
	public Response getMsg(@PathParam("objectId") String objectId,
			@PathParam("path") String path,
			@Suspended final AsyncResponse asyncResponse)
			throws org.json.JSONException {

		final StringBuilder response = new StringBuilder();

		asyncResponse.setTimeoutHandler(new TimeoutHandler() {

			@Override
			public void handleTimeout(AsyncResponse asyncResponse) {
				asyncResponse.resume(Response
						.status(Response.Status.SERVICE_UNAVAILABLE)
						.entity("Operation time out [5 sec], successCallback doesn't invoke!!").build());
			}
		});
		asyncResponse.setTimeout(5, TimeUnit.SECONDS);

		Pubnub pubnub = new Pubnub("pub-69159aa7-3bcf-4d09-ae25-3269f14acb6a",
				"sub-4d81bf51-1eb6-11e1-82b2-3d61f7276a67");
		pubnub.setCacheBusting(false);
		pubnub.setOrigin("pubsub-beta");

		final PubnubSyncedObject myData = pubnub.createSyncObject(objectId,
				path);

		try {
			myData.initSync(new Callback() {

				// Called when the initialization process connects the ObjectID
				// to PubNub
				@Override
				public void connectCallback(String channel, Object message) {
					System.out.println("Object Initialized : " + message);

					try {

						System.err.println(myData.toString());
						System.out.println(myData.toString(2));
					} catch (org.json.JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				// Called every time ObjectID is changed, starting with the
				// initialization process
				// that retrieves current state of the object
				@Override
				public void successCallback(String channel, Object message) {

					response.append("initSync()-> successCallback -> print message: "
							+ message.toString() + "</br>");
					asyncResponse.resume(response.toString());

				}

				// Called whenever any error occurs
				@Override
				public void errorCallback(String channel, PubnubError error) {
					response.append(System.currentTimeMillis() / 1000
							+ " : "

							+ error);

					asyncResponse.resume(response.toString());
				}

			});
		} catch (PubnubException e) {
			e.printStackTrace();
		}

		return Response.status(200).entity("").build();

	}
}
