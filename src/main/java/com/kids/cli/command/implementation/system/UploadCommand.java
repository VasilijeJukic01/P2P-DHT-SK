package com.kids.cli.command.implementation.system;

import com.kids.app.AppConfig;
import com.kids.cli.command.CLICommand;
import com.kids.file.FileOperations;
import com.kids.app.servent.ServentIdentity;

import java.util.List;

public class UploadCommand implements CLICommand {

    @Override
    public String commandName() {
        return "upload";
    }

    @Override
    public void execute(String args) {
        String[] parts = args.split(" ");

        if (parts.length != 1) {
            AppConfig.timestampedErrorPrint("Invalid number of arguments for Upload Command.");
            return;
        }

        String path = parts[0];

        if (!FileOperations.isImageFile(path)) {
            AppConfig.timestampedErrorPrint("Invalid file type. Only image files (jpg, jpeg, png, gif, bmp, tiff, webp, svg) are allowed.");
            return;
        }

        if (FileOperations.isFile(AppConfig.ROOT_DIR, path)) {
            int key = FileOperations.hashFilePath(path);

            List<ServentIdentity> serventIdentity = AppConfig.chordState.getAllNodeInfo().stream()
                            .map(n -> new ServentIdentity(n.getIpAddress(), n.getListenerPort()))
                            .toList();

            AppConfig.chordState.getMutex().lock(serventIdentity, false);

            AppConfig.timestampedStandardPrint("Initiating upload of image: " + path);

            AppConfig.chordState.getSystemManager().upload(
                    key,
                    path,
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort()
            );
        }
        else {
            AppConfig.timestampedErrorPrint("Invalid file path in upload command: " + path);
        }
    }
}