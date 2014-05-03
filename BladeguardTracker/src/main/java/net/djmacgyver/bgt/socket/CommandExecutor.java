package net.djmacgyver.bgt.socket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;

public class CommandExecutor extends AsyncTask <SocketCommand, Object, SocketCommand> {
    private final Context context;
    private SocketCommand command;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ((SocketService.LocalBinder) iBinder).getService().getSharedConnection().sendCommand(command);
            context.unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    public CommandExecutor(Context context) {
        this.context = context;
    }

    @Override
    protected SocketCommand doInBackground(SocketCommand... socketCommands) {
        command = socketCommands[0];
        Intent i = new Intent(context, SocketService.class);
        context.bindService(i, conn, Context.BIND_AUTO_CREATE);
        return command;
    }
}
