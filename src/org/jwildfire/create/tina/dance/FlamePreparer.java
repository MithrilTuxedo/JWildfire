/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2011 Andreas Maschke

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
package org.jwildfire.create.tina.dance;

import org.jwildfire.base.Prefs;
import org.jwildfire.create.tina.base.Flame;

public class FlamePreparer {
  private final Prefs prefs;

  public FlamePreparer(Prefs pPrefs) {
    prefs = pPrefs;
  }

  public void prepareFlame(Flame pFlame) {
    pFlame.setBGTransparency(false);
    pFlame.setGamma(1.5);
    pFlame.setBrightness(3.36);
    pFlame.getPalette().setModRed(90);
    pFlame.getPalette().setModRed(60);
    pFlame.getPalette().setModBlue(-60);
    pFlame.setSampleDensity(2 * prefs.getTinaRenderRealtimeQuality());
    pFlame.setSpatialFilterRadius(0.75);
    pFlame.setSpatialOversample(1);
    pFlame.setColorOversample(1);
  }

}
