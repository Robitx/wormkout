#ifdef GL_ES
precision lowp float;
#endif

uniform sampler2D u_texture;


uniform float u_time;
uniform vec2 u_resolution;

varying vec4 v_color;
varying vec2 v_texCoord0;



void main() {


    gl_FragColor = v_color * texture2D(u_texture, v_texCoord0);


		float s = 0.0, v = 0.0;
    	vec2 uv = (gl_FragCoord.xy / u_resolution.xy) * 2.0 - 1.0;
    	float t = u_time*0.005;
    	//uv.x = (uv.x * u_resolution.x / u_resolution.y) + sin(t) * 0.5;
    	uv.x = (uv.x * u_resolution.x / u_resolution.y);

    	//float si = sin(t + 2.17); // ...Squiffy rotation matrix!
    	//float co = cos(t);
    	//uv *= mat2(co, si, -si, co);
    	vec3 col = vec3(0.0);
    	//vec3 init = vec3(0.25, 0.25 + sin(u_time * 0.001) * 0.4, floor(u_time) * 0.0008);
    	vec3 init = vec3(0.25, 0.25, floor(u_time)*0.0005);
    	for (int r = 0; r < 2; r++)
    	{
    		vec3 p = init + s * vec3(uv, 0.1);
    		p.z = mod(p.z, 1.0);
    		//p.z = 0.1;
    		for (int i=0; i < 9; i++)	p = abs(p * 1.618 / dot(p, p) - vec3(0.718,0.58,0.78));
    		v += length(p * p) * smoothstep(0.0, 0.5, 0.9 - s) * .002;
    		// Get a purple and cyan effect by biasing the RGB in different ways...
    		col +=  vec3(v * 0.8, 1.1 - s * 0.5, .7 + v * 0.5) * v * 0.013;
    		s += .01;
    	}
    	gl_FragColor = vec4(col, 1.0);
}