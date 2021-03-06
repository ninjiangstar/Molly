package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import classes.Channel;
import server.ChannelManager;

@WebServlet("/channels")
public class ChannelsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ChannelsServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// set headers
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		// get parameters
		// String channelId = request.getParameter("id");

		JSONArray channelsArr = new JSONArray();
		for (Channel ch : ChannelManager.channels.values()) {
			if (ch.isLive) {
				channelsArr.add(ch.channelId);
			}
		}

		JSONObject obj = new JSONObject();
		obj.put("count", ChannelManager.size());
		obj.put("channels", channelsArr);

		out.print(obj.toJSONString());
		out.flush();
	}

}
