package com.hirshi001.game.keybaord;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.hirshi001.game.MainGame;

public class KeyboardActor extends Table {

    private static final char[] chars = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public KeyboardActor() {
        super();

        Skin skin = MainGame.INSTANCE().uiSkin;
        for(int i = 1; i<=9; i++){
            add(new Key(i+"", skin, Input.Keys.valueOf(""+i))).fill().grow().minWidth(20F);
        }
        add(new Key("0", skin, Input.Keys.NUM_0)).fill();
        add(new Key("Backspace", skin, Input.Keys.BACKSPACE)).colspan(2).fill().grow().minWidth(20F);

        row();
        int count = 0;
        Table row2 = new Table();
        for(int i=0;i<2;i++){
            for(int j=0;j<10;j++){
                String c = chars[count]+"";
                row2.add(new Key(c, skin, Input.Keys.valueOf(c))).fill().grow().minWidth(20F);
                count++;
            }
            row2.row();
        }
        add(row2).colspan(10).fill();
        add(new Key("Enter", skin, Input.Keys.ENTER)).colspan(2).fill().grow().minWidth(20F);

        row();

        System.out.println(chars.length);
        System.out.println(count);
        add().colspan(4);
        for(;count<chars.length;count++){
            String c = chars[count]+"";
            add(new Key(c, skin, Input.Keys.valueOf(c))).fill().grow().minWidth(20F);
        }
        add(new Key("Space", skin, Input.Keys.SPACE)).colspan(2).grow().fill();
    }

    public void setKeyListener(KeyListener listener){
        for(Actor a : getChildren()){
            if(a instanceof Key){
                ((Key) a).setKeyListener(listener);
            }
            if(a instanceof Table){
                for(Actor b : ((Table) a).getChildren()){
                    if(b instanceof Key){
                        ((Key) b).setKeyListener(listener);
                    }
                }
            }
        }
    }


}
