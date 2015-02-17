precision mediump float;

uniform sampler2D u_Texture;

varying vec4 v_Color;

varying vec2 v_TexCoordinate;

void main() {
	if(v_TexCoordinate[0] < -0.5){
		gl_FragColor = v_Color;
	}else{
    	gl_FragColor = v_Color * texture2D(u_Texture, v_TexCoordinate);
    }
}
