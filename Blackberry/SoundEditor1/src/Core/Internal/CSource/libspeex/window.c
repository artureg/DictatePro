/* Copyright (C) 2006 Jean-Marc Valin 
 File: window.c

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 - Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 
 - Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 
 - Neither the name of the Xiph.org Foundation nor the names of its
 contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "arch.h"

#ifdef FIXED_POINT
const spx_word16_t lag_window[11] = {
	16384, 16337, 16199, 15970, 15656, 15260, 14790, 14254, 13659, 13015, 12330
};

const spx_word16_t lpc_window[200] = {
	1310, 1313, 1321, 1333, 1352, 1375, 1403, 1436,
	1475, 1518, 1567, 1621, 1679, 1743, 1811, 1884,
	1962, 2044, 2132, 2224, 2320, 2421, 2526, 2636,
	2750, 2868, 2990, 3116, 3246, 3380, 3518, 3659,
	3804, 3952, 4104, 4259, 4417, 4578, 4742, 4909,
	5079, 5251, 5425, 5602, 5781, 5963, 6146, 6331,
	6518, 6706, 6896, 7087, 7280, 7473, 7668, 7863,
	8059, 8256, 8452, 8650, 8847, 9044, 9241, 9438,
	9635, 9831, 10026, 10220, 10414, 10606, 10797, 10987,
	11176, 11363, 11548, 11731, 11912, 12091, 12268, 12443,
	12615, 12785, 12952, 13116, 13277, 13435, 13590, 13742,
	13890, 14035, 14176, 14314, 14448, 14578, 14704, 14826,
	14944, 15058, 15168, 15273, 15374, 15470, 15562, 15649,
	15732, 15810, 15883, 15951, 16015, 16073, 16127, 16175,
	16219, 16257, 16291, 16319, 16342, 16360, 16373, 16381,
	16384, 16384, 16384, 16384, 16384, 16384, 16384, 16384,
	16384, 16384, 16384, 16384, 16384, 16384, 16384, 16384,
	16384, 16384, 16384, 16384, 16384, 16384, 16384, 16384,
	16384, 16384, 16384, 16384, 16384, 16384, 16384, 16384,
	16384, 16384, 16384, 16384, 16384, 16384, 16384, 16384,
	16384, 16384, 16384, 16384, 16384, 16384, 16384, 16384,
	16384, 16384, 16384, 16361, 16294, 16183, 16028, 15830,
	15588, 15304, 14979, 14613, 14207, 13763, 13282, 12766,
	12215, 11631, 11016, 10373, 9702, 9007, 8289, 7551,
	6797, 6028, 5251, 4470, 3695, 2943, 2248, 1696
};
#else
const spx_word16_t lag_window[11] = { 1.00000, 0.99716, 0.98869, 0.97474,
		0.95554, 0.93140, 0.90273, 0.86998, 0.83367, 0.79434, 0.75258 };

const spx_word16_t lpc_window[200] = { 0.080000f, 0.080158f, 0.080630f,
		0.081418f, 0.082520f, 0.083935f, 0.085663f, 0.087703f, 0.090052f,
		0.092710f, 0.095674f, 0.098943f, 0.102514f, 0.106385f, 0.110553f,
		0.115015f, 0.119769f, 0.124811f, 0.130137f, 0.135744f, 0.141628f,
		0.147786f, 0.154212f, 0.160902f, 0.167852f, 0.175057f, 0.182513f,
		0.190213f, 0.198153f, 0.206328f, 0.214731f, 0.223357f, 0.232200f,
		0.241254f, 0.250513f, 0.259970f, 0.269619f, 0.279453f, 0.289466f,
		0.299651f, 0.310000f, 0.320507f, 0.331164f, 0.341965f, 0.352901f,
		0.363966f, 0.375151f, 0.386449f, 0.397852f, 0.409353f, 0.420943f,
		0.432615f, 0.444361f, 0.456172f, 0.468040f, 0.479958f, 0.491917f,
		0.503909f, 0.515925f, 0.527959f, 0.540000f, 0.552041f, 0.564075f,
		0.576091f, 0.588083f, 0.600042f, 0.611960f, 0.623828f, 0.635639f,
		0.647385f, 0.659057f, 0.670647f, 0.682148f, 0.693551f, 0.704849f,
		0.716034f, 0.727099f, 0.738035f, 0.748836f, 0.759493f, 0.770000f,
		0.780349f, 0.790534f, 0.800547f, 0.810381f, 0.820030f, 0.829487f,
		0.838746f, 0.847800f, 0.856643f, 0.865269f, 0.873672f, 0.881847f,
		0.889787f, 0.897487f, 0.904943f, 0.912148f, 0.919098f, 0.925788f,
		0.932214f, 0.938372f, 0.944256f, 0.949863f, 0.955189f, 0.960231f,
		0.964985f, 0.969447f, 0.973615f, 0.977486f, 0.981057f, 0.984326f,
		0.987290f, 0.989948f, 0.992297f, 0.994337f, 0.996065f, 0.997480f,
		0.998582f, 0.999370f, 0.999842f, 1.000000f, 1.000000f, 1.000000f,
		1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f,
		1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f,
		1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f,
		1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f,
		1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f,
		1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f,
		1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f,
		1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f, 1.000000f,
		0.998640f, 0.994566f, 0.987787f, 0.978324f, 0.966203f, 0.951458f,
		0.934131f, 0.914270f, 0.891931f, 0.867179f, 0.840084f, 0.810723f,
		0.779182f, 0.745551f, 0.709930f, 0.672424f, 0.633148f, 0.592223f,
		0.549781f, 0.505964f, 0.460932f, 0.414863f, 0.367968f, 0.320511f,
		0.272858f, 0.225569f, 0.179655f, 0.137254f, 0.103524f };
#endif
