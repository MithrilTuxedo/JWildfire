/*
 JWildfireC - an external C-based fractal-flame-renderer for JWildfire
 Copyright (C) 2012 Andreas Maschke

 This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 General Public License as published by the Free Software Foundation; either version 2.1 of the
 License, or (at your option) any later version.
 
 This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License along with this software;
 if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

#include "jwf_Constants.h"
#include "jwf_Variation.h"

class Hexaplay3DFunc: public Variation {
public:
	Hexaplay3DFunc() {
	  majp = 1.0;
	  scale = 0.25;
	  zlift = 0.25;
		initParameterNames(3, "majp", "scale", "zlift");
	}

	const char* getName() const {
		return "hexaplay3D";
	}

	void setParameter(char *pName, JWF_FLOAT pValue) {
		if (strcmp(pName, "majp") == 0) {
			majp = pValue;
		}
		else if (strcmp(pName, "scale") == 0) {
			scale = pValue;
		}
		else if (strcmp(pName, "zlift") == 0) {
			zlift = pValue;
		}
	}

	void transform(FlameTransformationContext *pContext, XForm *pXForm, XYZPoint *pAffineTP, XYZPoint *pVarTP, JWF_FLOAT pAmount) {
    /* hexaplay3D by Larry Berlin, http://aporev.deviantart.com/art/3D-Plugins-Collection-One-138514007?q=gallery%3Aaporev%2F8229210&qo=15 */
    if (_fcycle > 5) {
      _fcycle = 0;
      _rswtch = (int) JWF_TRUNC((JWF_FLOAT)(pContext->randGen->random() * 3.0)); //  Chooses new 6 or 3 nodes
    }
    if (_bcycle > 2) {
      _bcycle = 0;
      _rswtch = (int) JWF_TRUNC((JWF_FLOAT)(pContext->randGen->random() * 3.0)); //  Chooses new 6 or 3 nodes
    }

    JWF_FLOAT lrmaj = pAmount; // Sets hexagon length radius - major plane
    JWF_FLOAT boost = 0; //  Boost is the separation distance between the two planes
    int posNeg = 1;
    int loc60;
    int loc120;
    JWF_FLOAT lscale = scale * 0.5;

    if (pContext->randGen->random() < 0.5) {
      posNeg = -1;
    }

    // Determine whether one or two major planes
    int majplane = 1;
    JWF_FLOAT abmajp = JWF_FABS(majp);
    if (abmajp <= 1.0) {
      majplane = 1; // Want either 1 or 2
    }
    else {
      majplane = 2;
      boost = (abmajp - 1.0) * 0.5; // distance above and below XY plane
    }

    //      Creating Z factors relative to the planes
    if (majplane == 2) {
      pVarTP->z += pAffineTP->z * 0.5 * zlift + (posNeg * boost);
    }
    else {
      pVarTP->z += pAffineTP->z * 0.5 * zlift;
    }

    // Work out the segments and hexagonal nodes
    if (_rswtch <= 1) { // Occasion to build using 60 degree segments
      //loc60 = trunc(pContext.random()*6.0);  // random nodes selection
      loc60 = _fcycle; // sequential nodes selection
      pVarTP->x = ((pVarTP->x + pAffineTP->x) * lscale) + (lrmaj * _seg60x[loc60]);
      pVarTP->y = ((pVarTP->y + pAffineTP->y) * lscale) + (lrmaj * _seg60y[loc60]);
      _fcycle += 1;
    }
    else {// Occasion to build on 120 degree segments

      //loc120 = trunc(pContext.random()*3.0);  // random nodes selection
      loc120 = _bcycle; // sequential nodes selection
      pVarTP->x = ((pVarTP->x + pAffineTP->x) * lscale) + (lrmaj * _seg120x[loc120]);
      pVarTP->y = ((pVarTP->y + pAffineTP->y) * lscale) + (lrmaj * _seg120y[loc120]);
      _bcycle += 1;
    }
	}

	Hexaplay3DFunc* makeCopy() {
		return new Hexaplay3DFunc(*this);
	}

	virtual void init(FlameTransformationContext *pContext, XForm *pXForm, JWF_FLOAT pAmount) {
    /*  Set up two major angle systems */
    _rswtch = (int) JWF_TRUNC((JWF_FLOAT)(pContext->randGen->random() * 3.0)); //  Chooses 6 or 3 nodes
    JWF_FLOAT hlift = JWF_SIN(M_PI / 3.0);
    _fcycle = 0;
    _bcycle = 0;
    _seg60x[0] = 1.0;
    _seg60x[1] = 0.5;
    _seg60x[2] = -0.5;
    _seg60x[3] = -1.0;
    _seg60x[4] = -0.5;
    _seg60x[5] = 0.5;

    _seg60y[0] = 0.0;
    _seg60y[1] = hlift;
    _seg60y[2] = hlift;
    _seg60y[3] = 0.0;
    _seg60y[4] = -hlift;
    _seg60y[5] = -hlift;

    _seg120x[0] = 1.0;
    _seg120x[1] = -0.5;
    _seg120x[2] = -0.5;

    _seg120y[0] = 0.0;
    _seg120y[1] = hlift;
    _seg120y[2] = -hlift;
	}

private:
  JWF_FLOAT majp; // establishes 1 or 2 planes, and if 2, the distance between them
  JWF_FLOAT scale; // scales the effect of X and Y
  JWF_FLOAT zlift; // scales the effect of Z axis within the snowflake

  JWF_FLOAT _seg60x[6];
  JWF_FLOAT _seg60y[6];
  JWF_FLOAT _seg120x[3];
  JWF_FLOAT _seg120y[3];
  int _rswtch; //  for choosing between 6 or 3 segments to a plane
  int _fcycle; //  markers to count cycles...
  int _bcycle;
};
