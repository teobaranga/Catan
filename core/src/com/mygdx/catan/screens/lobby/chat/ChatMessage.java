package com.mygdx.catan.screens.lobby.chat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class ChatMessage extends Table {

    private static final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

    private Label sender;
    private Label message;

    public ChatMessage(String sender, String message) {
        super(skin);
//        debugAll();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DroidSans.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 17;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        final Label.LabelStyle labelStyle = skin.get("chat-sender", Label.LabelStyle.class);
        labelStyle.font = generator.generateFont(parameter);
        this.sender = new Label(sender, labelStyle);
        this.message = new Label(message, skin.get("chat-message", Label.LabelStyle.class));
        this.message.setWrap(true);
        add(this.sender).fillX().expandX();
        row();
        add(this.message).fillX().expandX();

        generator.dispose(); // don't forget to dispose to avoid memory leaks!
    }
}
