/**
 * Service type configuration with skill requirements.
 */
class Service {
    private String name;
    private int T, C, R, E, A;

    public Service(String name, int T, int C, int R, int E, int A) {
        this.name = name;
        this.T = T;
        this.C = C;
        this.R = R;
        this.E = E;
        this.A = A;
    }

    public String getName() { return name; }
    public int getT() { return T; }
    public int getC() { return C; }
    public int getR() { return R; }
    public int getE() { return E; }
    public int getA() { return A; }

    public int[] getSkillProfile() {
        return new int[]{T, C, R, E, A};
    }

    public int getPrimarySkill() {
        // Find max value, tie-break by order: T(0), C(1), R(2), E(3), A(4)
        // Lower index wins in tie
        int max = T;
        int idx = 0;
        if (C > max) { max = C; idx = 1; }
        if (R > max) { max = R; idx = 2; }
        if (E > max) { max = E; idx = 3; }
        if (A > max) { max = A; idx = 4; }
        return idx;
    }

    public int[] getSecondarySkills() {
        // Create array of (value, index) pairs
        int[][] pairs = {
            {T, 0}, {C, 1}, {R, 2}, {E, 3}, {A, 4}
        };
        
        // Sort by value descending, then by index ascending (T, C, R, E, A order)
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                if (pairs[j][0] > pairs[i][0] || 
                    (pairs[j][0] == pairs[i][0] && pairs[j][1] < pairs[i][1])) {
                    int[] temp = pairs[i];
                    pairs[i] = pairs[j];
                    pairs[j] = temp;
                }
            }
        }
        
        // Return indices of 2nd and 3rd highest
        return new int[]{pairs[1][1], pairs[2][1]};
    }
}

