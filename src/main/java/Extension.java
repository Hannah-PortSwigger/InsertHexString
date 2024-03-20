import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;

import javax.swing.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Extension implements BurpExtension
{

    private final String EXTENSION_TITLE= "Insert Hex String";

    @Override
    public void initialize(MontoyaApi montoyaApi)
    {
        montoyaApi.extension().setName(EXTENSION_TITLE);

        montoyaApi.userInterface().registerContextMenuItemsProvider(contextMenuEvent ->
        {
            if (contextMenuEvent.isFromTool(ToolType.REPEATER))
            {
                JMenuItem menuItem = new JMenuItem(EXTENSION_TITLE);
                menuItem.addActionListener(l -> {
                    String input = JOptionPane.showInputDialog(
                            montoyaApi.userInterface().swingUtils().suiteFrame(),
                            "Hex string to insert",
                            EXTENSION_TITLE,
                            JOptionPane.PLAIN_MESSAGE
                    );

                    ByteArray data = convertHexStringToByteArray(input);

                    if (contextMenuEvent.messageEditorRequestResponse().isPresent())
                    {
                        MessageEditorHttpRequestResponse messageEditorHttpRequestResponse = contextMenuEvent.messageEditorRequestResponse().get();

                        int caretPosition = messageEditorHttpRequestResponse.caretPosition();

                        HttpRequest originalRequest = messageEditorHttpRequestResponse.requestResponse().request();

                        ByteArray newByteArray = concatenateByteArray(originalRequest.toByteArray(), caretPosition, data);

                        messageEditorHttpRequestResponse.setRequest(HttpRequest.httpRequest(originalRequest.httpService(), newByteArray));
                    }
                    else
                    {
                        montoyaApi.logging().logToOutput("Can't find message editor.");
                    }
                });

                return singletonList(menuItem);
            }
            return emptyList();
        });
    }

    private static ByteArray concatenateByteArray(ByteArray originalBytes, int offset, ByteArray dataToInsert)
    {
        ByteArray prependArray = originalBytes.subArray(0, offset);
        ByteArray postpendArray = originalBytes.subArray(offset, originalBytes.length());

        return prependArray.withAppended(dataToInsert).withAppended(postpendArray);
    }

    private ByteArray convertHexStringToByteArray(String hexString)
    {
        String strippedHexString = hexString.replaceAll(" ", "");

        int len = strippedHexString.length();

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(strippedHexString.charAt(i), 16) << 4)
                    + Character.digit(strippedHexString.charAt(i+1), 16));
        }

        return ByteArray.byteArray(data);
    }
}
