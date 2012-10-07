#ifdef HARDWARE_SHADOWS
    #define SHADOWMAP sampler2DShadow
    #define SHADOWCOMPARE(tex,coord) shadow2DProj(tex, coord).r 
#else
    #define SHADOWMAP sampler2D
    #define SHADOWCOMPARE(tex,coord) step(coord.z, texture2DProj(tex, coord).r)
#endif

#if FILTER_MODE == 0
    #define GETSHADOW Shadow_DoShadowCompare
    #define KERNEL 1.0
#elif FILTER_MODE == 1
    #ifdef HARDWARE_SHADOWS
        #define GETSHADOW Shadow_DoShadowCompare
    #else
        #define GETSHADOW Shadow_DoBilinear_2x2
    #endif
    #define KERNEL 1.0
#elif FILTER_MODE == 2
    #define GETSHADOW Shadow_DoDither_2x2
    #define KERNEL 1.0
#elif FILTER_MODE == 3
    #define GETSHADOW Shadow_DoPCF
    #define KERNEL 4.0
#elif FILTER_MODE == 4
    #define GETSHADOW Shadow_DoPCFPoisson
    #define KERNEL 4
#elif FILTER_MODE == 5
    #define GETSHADOW Shadow_DoPCF
    #define KERNEL 8.0
#endif


uniform SHADOWMAP m_ShadowMap0;
uniform SHADOWMAP m_ShadowMap1;
uniform SHADOWMAP m_ShadowMap2;
uniform SHADOWMAP m_ShadowMap3;

uniform vec4 m_Splits;

uniform float m_ShadowIntensity;

varying vec4 projCoord0;
varying vec4 projCoord1;
varying vec4 projCoord2;
varying vec4 projCoord3;

varying float shadowPosition;


const vec2 pixSize2 = vec2(1.0 / SHADOWMAP_SIZE);
float scale = 1.0;

float Shadow_DoShadowCompareOffset(in SHADOWMAP tex, in vec4 projCoord, in vec2 offset){
    vec4 coord = vec4(projCoord.xy + offset.xy * pixSize2, projCoord.zw);
    return SHADOWCOMPARE(tex, coord);
}

float Shadow_DoShadowCompare(in SHADOWMAP tex, vec4 projCoord){
    return SHADOWCOMPARE(tex, projCoord);
}

float Shadow_BorderCheck(in vec2 coord){
    // Fastest, "hack" method (uses 4-5 instructions)
    vec4 t = vec4(coord.xy, 0.0, 1.0);
    t = step(t.wwxy, t.xyzz);
    return dot(t,t);
}

float Shadow_DoDither_2x2(in SHADOWMAP tex, in vec4 projCoord){
    float border = Shadow_BorderCheck(projCoord.xy);
    if (border > 0.0)
        return 1.0;
  

    float shadow = 0.0;
    vec2 o = mod(floor(gl_FragCoord.xy), 2.0);
    shadow += Shadow_DoShadowCompareOffset(tex,projCoord,vec2(-1.5,  1.5) + o);
    shadow += Shadow_DoShadowCompareOffset(tex,projCoord,vec2( 0.5,  1.5) + o);
    shadow += Shadow_DoShadowCompareOffset(tex,projCoord,vec2(-1.5, -0.5) + o);
    shadow += Shadow_DoShadowCompareOffset(tex,projCoord,vec2( 0.5, -0.5) + o);
    shadow *= 0.25 ;
    return shadow;
}

float Shadow_DoBilinear_2x2(in SHADOWMAP tex, in vec4 projCoord){
    float border = Shadow_BorderCheck(projCoord.xy);
    if (border > 0.0)
        return 1.0;
    vec4 gather = vec4(0.0);
    gather.x = Shadow_DoShadowCompareOffset(tex, projCoord, vec2(0.0, 0.0));
    gather.y = Shadow_DoShadowCompareOffset(tex, projCoord, vec2(1.0, 0.0));
    gather.z = Shadow_DoShadowCompareOffset(tex, projCoord, vec2(0.0, 1.0));
    gather.w = Shadow_DoShadowCompareOffset(tex, projCoord, vec2(1.0, 1.0));

    vec2 f = fract( projCoord.xy * SHADOWMAP_SIZE );
    vec2 mx = mix( gather.xz, gather.yw, f.x );
    return mix( mx.x, mx.y, f.y );
}

float Shadow_DoPCF(in SHADOWMAP tex, in vec4 projCoord){
    float shadow = 0.0;
    float border = Shadow_BorderCheck(projCoord.xy);
    if (border > 0.0)
        return 1.0;
    float bound = KERNEL * 0.5 - 0.5;
    bound *= PCFEDGE;
    for (float y = -bound; y <= bound; y += PCFEDGE){
        for (float x = -bound; x <= bound; x += PCFEDGE){
            shadow += clamp(Shadow_DoShadowCompareOffset(tex,projCoord,vec2(x,y)) +
                            border,
                            0.0, 1.0);
        }
    }

    shadow = shadow / (KERNEL * KERNEL);
    return shadow;
}


//12 tap poisson disk
/*
vec2 poissonDisk[12] =vec2[12]( vec2(-0.1711046, -0.425016),
 vec2(-0.7829809, 0.2162201),
 vec2(-0.2380269, -0.8835521),
 vec2(0.4198045, 0.1687819),
 vec2(-0.684418, -0.3186957),
 vec2(0.6026866, -0.2587841),
 vec2(-0.2412762, 0.3913516),
 vec2(0.4720655, -0.7664126),
 vec2(0.9571564, 0.2680693),
 vec2(-0.5238616, 0.802707),
 vec2(0.5653144, 0.60262),
 vec2(0.0123658, 0.8627419));
*/
    const vec2 poissonDisk0 =  vec2(-0.1711046, -0.425016);
    const vec2 poissonDisk1 =  vec2(-0.7829809, 0.2162201);
    const vec2 poissonDisk2 =  vec2(-0.2380269, -0.8835521);
    const vec2 poissonDisk3 =  vec2(0.4198045, 0.1687819);
    const vec2 poissonDisk4 =  vec2(-0.684418, -0.3186957);
    const vec2 poissonDisk5 =  vec2(0.6026866, -0.2587841);
    const vec2 poissonDisk6 =  vec2(-0.2412762, 0.3913516);
    const vec2 poissonDisk7 =  vec2(0.4720655, -0.7664126);
    const vec2 poissonDisk8 =  vec2(0.9571564, 0.2680693);
    const vec2 poissonDisk9 =  vec2(-0.5238616, 0.802707);
    const vec2 poissonDisk10 = vec2(0.5653144, 0.60262);
    const vec2 poissonDisk11 = vec2(0.0123658, 0.8627419);

float Shadow_DoPCFPoisson(in SHADOWMAP tex, in vec4 projCoord){   
    float shadow = 0.0;
    float border = Shadow_BorderCheck(projCoord.xy);
    if (border > 0.0)
        return 1.0;

    vec2 texelSize = vec2( 4.0 * PCFEDGE * scale);        
    
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk0 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk1 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk2 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk3 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk4 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk5 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk6 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk7 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk8 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk9 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk10 * texelSize);
     shadow += Shadow_DoShadowCompareOffset(tex, projCoord , poissonDisk11 * texelSize);

    shadow = shadow * 0.08333333333;//this is divided by 12
    return shadow;
}

#ifdef DISCARD_ALPHA
    #ifdef COLOR_MAP
        uniform sampler2D m_ColorMap;
    #else    
        uniform sampler2D m_DiffuseMap;
    #endif
    uniform float m_AlphaDiscardThreshold;
    varying vec2 texCoord;
#endif

void main(){   
 
    #ifdef DISCARD_ALPHA
        #ifdef COLOR_MAP
            float alpha = texture2D(m_ColorMap,texCoord).a;
        #else    
            float alpha = texture2D(m_DiffuseMap,texCoord).a;
        #endif
        if(alpha<=m_AlphaDiscardThreshold){
            discard;
        }

    #endif
   

    vec4 shadowPerSplit = vec4(0.0);
float shadow;
//shadowPosition
    if(shadowPosition < m_Splits.x){
        shadow= GETSHADOW(m_ShadowMap0, projCoord0);
    }else if( shadowPosition <  m_Splits.y){
        scale = 0.5;
        shadow = GETSHADOW(m_ShadowMap1, projCoord1);
    }else if( shadowPosition <  m_Splits.z){
        scale = 0.25;
        shadow= GETSHADOW(m_ShadowMap2, projCoord2);
    }else if( shadowPosition <  m_Splits.w){
        scale = 0.125;
        shadow= GETSHADOW(m_ShadowMap3, projCoord3);
    }
/*    
shadowPerSplit.x = GETSHADOW(m_ShadowMap0, projCoord0);
    shadowPerSplit.y = GETSHADOW(m_ShadowMap1, projCoord1);
    shadowPerSplit.z = GETSHADOW(m_ShadowMap2, projCoord2);
    shadowPerSplit.w = GETSHADOW(m_ShadowMap3, projCoord3);
*/
 
/*
    vec4 less = step( shadowPosition, m_Splits );
    vec4 more = vec4(1.0) - step( shadowPosition, vec4(0.0, m_Splits.xyz) );
    float shadow = dot(shadowPerSplit, less * more );
  */  
    shadow = shadow * m_ShadowIntensity + (1.0 - m_ShadowIntensity);
  

  gl_FragColor = vec4(shadow, shadow, shadow, 1.0);

}

