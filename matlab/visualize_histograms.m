function retval = visualize_histograms (f_abcd, bins, proportion) 

syms a b c d x y z

X_abcd = load('X160_one.txt');

if proportion ~= -1
    idX_abcd = find( abs(proportion - (X_abcd(:,1) + X_abcd(:,3)) ./ (X_abcd(:,1) + X_abcd(:,2) + X_abcd(:,3) + X_abcd(:,4))) <= 0.0125 );
    X_abcd = X_abcd(idX_abcd, :);
end

f = @(a,b,c,d)(eval(f_abcd));
hist_vals = f(X_abcd(:,1), X_abcd(:,2), X_abcd(:,3), X_abcd(:,4));

% histfit(hist_vals, bins, 'kernel')
retval = histogram(hist_vals, bins, 'Normalization', 'probability', 'EdgeColor', [ 0 0 0], 'FaceColor', [ 0 0 0]);
return;
