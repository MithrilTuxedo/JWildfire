/*
  JWildfire - an image and animation processor written in Java 
  Copyright (C) 1995-2014 Andreas Maschke

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
package org.jwildfire.create.tina.base;

import static org.jwildfire.base.mathlib.MathLib.EPSILON;
import static org.jwildfire.base.mathlib.MathLib.fabs;

import java.io.Serializable;

import org.jwildfire.create.tina.edit.Assignable;
import org.jwildfire.envelope.Envelope;
import org.jwildfire.envelope.Envelope.Interpolation;

public class MotionCurve implements Serializable, Assignable<MotionCurve> {
  private static final long serialVersionUID = 1L;

  private boolean enabled;
  private int viewXMin = -10;
  private int viewXMax = 310;
  private double viewYMin = -120.0;
  private double viewYMax = 120.0;
  private Interpolation interpolation = Interpolation.SPLINE;
  private int selectedIdx = 0;
  private boolean locked;
  private int[] x = new int[] {};
  private double[] y = new double[] {};

  public void assignFromEnvelope(Envelope pEnvelope) {
    viewXMin = pEnvelope.getViewXMin();
    viewXMax = pEnvelope.getViewXMax();
    viewYMin = pEnvelope.getViewYMin();
    viewYMax = pEnvelope.getViewYMax();
    interpolation = pEnvelope.getInterpolation();
    selectedIdx = pEnvelope.getSelectedIdx();
    locked = pEnvelope.isLocked();
    x = new int[pEnvelope.getX().length];
    System.arraycopy(pEnvelope.getX(), 0, x, 0, x.length);

    y = new double[pEnvelope.getY().length];
    System.arraycopy(pEnvelope.getY(), 0, y, 0, y.length);
  }

  public Envelope toEnvelope() {
    Envelope res = new Envelope();
    res.setViewXMin(viewXMin);
    res.setViewXMax(viewXMax);
    res.setViewYMin(viewYMin);
    res.setViewYMax(viewYMax);
    res.setInterpolation(interpolation);
    res.setSelectedIdx(selectedIdx);
    res.setLocked(locked);
    int[] newX = new int[x.length];
    System.arraycopy(x, 0, newX, 0, x.length);

    double[] newY = new double[y.length];
    System.arraycopy(y, 0, newY, 0, y.length);

    res.setValues(newX, newY);
    return res;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void assign(MotionCurve pSrc) {
    enabled = pSrc.enabled;
    viewXMin = pSrc.viewXMin;
    viewXMax = pSrc.viewXMax;
    viewYMin = pSrc.viewYMin;
    viewYMax = pSrc.viewYMax;
    interpolation = pSrc.interpolation;
    selectedIdx = pSrc.selectedIdx;
    locked = pSrc.locked;
    x = new int[pSrc.x.length];
    System.arraycopy(pSrc.x, 0, x, 0, x.length);
    y = new double[pSrc.y.length];
    System.arraycopy(pSrc.y, 0, y, 0, y.length);
  }

  @Override
  public MotionCurve makeCopy() {
    MotionCurve res = new MotionCurve();
    res.assign(this);
    return res;
  }

  @Override
  public boolean isEqual(MotionCurve pSrc) {
    if ((enabled != pSrc.enabled) || (fabs(viewXMin - pSrc.viewXMin) > EPSILON) ||
        (fabs(viewXMax - pSrc.viewXMax) > EPSILON) || (fabs(viewYMin - pSrc.viewYMin) > EPSILON) ||
        (fabs(viewYMax - pSrc.viewYMax) > EPSILON) || (interpolation != pSrc.interpolation) ||
        (selectedIdx != pSrc.selectedIdx) || (locked != pSrc.locked) ||
        (x.length != pSrc.x.length) || (y.length != pSrc.y.length)) {
      return false;
    }
    for (int i = 0; i < x.length; i++) {
      if (x[i] != pSrc.x[i]) {
        return false;
      }
    }
    for (int i = 0; i < y.length; i++) {
      if (fabs(y[i] - pSrc.y[i]) > EPSILON) {
        return false;
      }
    }
    return true;
  }

  public boolean isEmpty() {
    return x.length == 0;
  }
}
