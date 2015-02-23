precision highp float;

varying vec3 lightVec[2];
varying vec3 eyeVec;
varying vec2 texCoord;

//uniform sampler2D textureUnit0;
//uniform sampler2D textureUnit1;

uniform vec3 diffuseColors[8];
uniform vec3 specularColors[8];

uniform vec4 ambientColor;

uniform sampler2D videoFrame;
uniform sampler2D videoFrame2;
uniform sampler2D videoFrame3;

void main ()
{
/*
	vec4 vAmbient = ambientColor;
	vec3 vVec = normalize(eyeVec);
	
	float height = texture2D(textureUnit1, texCoord).a;
	vec2 offset = vVec.xy * (height * 2.0 - 1.0) *heightScale;
	vec2 newTexCoord = texCoord + offset;
	
	vec4 base = texture2D(textureUnit0, newTexCoord);
	vec3 bump = normalize(texture2D(textureUnit1, newTexCoord).xyz * 2.0 - 1.0);
	
	// First light source
	
	float distSqr = dot(lightVec[0], lightVec[0]);
	float att = clamp(1.0 - invRadius * sqrt(distSqr), 0.0, 1.0);
	vec3 lVec = lightVec[0] * inversesqrt(distSqr);

	float diffuse = max(dot(lVec, bump), 0.0);
	vec4 vDiffuse = vec4(diffuseColors[0],0) * diffuse;	

	float specular = pow(clamp(dot(reflect(-lVec, bump), vVec), 0.0, 1.0), 0.85);
	vec4 vSpecular = vec4(specularColors[0],0) * specular;	
*/

	//float red = texture2D(videoFrame, texCoord).r;//+texture2D(videoFrame2, texCoord).r;//+texture2D(videoFrame3, texCoord).r;
	//float green = texture2D(videoFrame, texCoord).g;//+texture2D(videoFrame2, texCoord).g;//+texture2D(videoFrame3, texCoord).g;
	//float blue = texture2D(videoFrame, texCoord).b;//+texture2D(videoFrame2, texCoord).b;//+texture2D(videoFrame3, texCoord).b;
	
	//gl_FragColor = vec4(red, green, blue, 1.0);//( vAmbient*base + vDiffuse*base + vSpecular) * att*2.0;
	
	
	vec2 tmp;
	tmp.x = texCoord.x * 2.0-0.5;
	tmp.y = texCoord.y * 2.0-0.5;
	
	if (tmp.x < 0.0 || tmp.y < 0.0) {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	} else {
		highp vec3 yuv;
		highp vec3 rgb;
		highp float nx, ny;
		nx = (tmp.x);
		ny = (tmp.y);
		yuv.x = texture2D(videoFrame, tmp).r;
		yuv.y = texture2D(videoFrame2, vec2(nx, ny)).r - 0.5;
		yuv.z = texture2D(videoFrame3, vec2(nx, ny)).r - 0.5;
		rgb = mat3(1, 1, 1, 0, -.34414, 1.772, 1.402, -.71414, 0) * yuv;
		gl_FragColor = vec4(rgb, 1);
	}
}
