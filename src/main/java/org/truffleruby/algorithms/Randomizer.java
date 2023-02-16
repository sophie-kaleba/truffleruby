/*
 ***** BEGIN LICENSE BLOCK *****
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.truffleruby.algorithms;

public class Randomizer {

    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = 0x9908b0df; /* constant vector a */
    private static final int UMASK = 0x80000000; /* most significant w-r bits */
    private static final int LMASK = 0x7fffffff; /* least significant r bits */

    private final int[] state = new int[N];
    private int left = 1;

    private final Object seed;

    public Randomizer() {
        this(0L, 0);
    }

    public Randomizer(Object seed, int s) {
        this.seed = seed;
        state[0] = s;
        for (int j = 1; j < N; j++) {
            state[j] = (1812433253 * (state[j - 1] ^ (state[j - 1] >>> 30)) + j);
        }
    }

    public Randomizer(Object seed, int[] initKey) {
        this(seed, 19650218);
        int len = initKey.length;
        int i = 1;
        int j = 0;
        int k = Math.max(N, len);
        for (; k > 0; k--) {
            state[i] = (state[i] ^ ((state[i - 1] ^ (state[i - 1] >>> 30)) * 1664525)) + initKey[j] + j;
            i++;
            j++;
            if (i >= N) {
                state[0] = state[N - 1];
                i = 1;
            }
            if (j >= len) {
                j = 0;
            }
        }
        for (k = N - 1; k > 0; k--) {
            state[i] = (state[i] ^ ((state[i - 1] ^ (state[i - 1] >>> 30)) * 1566083941)) - i;
            i++;
            if (i >= N) {
                state[0] = state[N - 1];
                i = 1;
            }
        }
        state[0] = 0x80000000;
    }

    public Object getSeed() {
        return seed;
    }

    // MRI: genrand_int32 of mt19937.c
    public int unsynchronizedGenrandInt32() {
        if (--left <= 0) {
            nextState();
        }

        int y = state[N - left];

        /* Tempering */
        y ^= (y >>> 11);
        y ^= (y << 7) & 0x9d2c5680;
        y ^= (y << 15) & 0xefc60000;
        y ^= (y >>> 18);

        return y;
    }

    private void nextState() {
        int p = 0;

        left = N;

        for (int j = N - M + 1; --j > 0; p++) {
            state[p] = state[p + M] ^ twist(state[p], state[p + 1]);
        }

        for (int j = M; --j > 0; p++) {
            state[p] = state[p + M - N] ^ twist(state[p], state[p + 1]);
        }

        state[p] = state[p + M - N] ^ twist(state[p], state[0]);
    }

    private static int mixbits(int u, int v) {
        return (u & UMASK) | (v & LMASK);
    }

    private static int twist(int u, int v) {
        return (mixbits(u, v) >>> 1) ^ (((v & 1) != 0) ? MATRIX_A : 0);
    }


}
